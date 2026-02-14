package sparkshow.services
import scala.concurrent.duration._

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std.Queue
import cats.syntax.all._
import sparkshow.data.Enqueued
import sparkshow.data.Failed
import sparkshow.data.New
import sparkshow.data.Running
import sparkshow.data.WaitingRetry
import sparkshow.db.models.Query
import sparkshow.db.models.Source
import sparkshow.db.repositories.MetricRepository
import sparkshow.db.repositories.QueryRepository
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.syntax._

class QueryQueueService(
    private val queryRepository: QueryRepository,
    private val metricRepository: MetricRepository,
    private val localSparkMetricCalcService: LocalSparkMetricCalcService
) {

    private final val MaxRetries = 3
    private given logger: SelfAwareStructuredLogger[IO] =
        Slf4jFactory.create[IO].getLogger

    def produceQueries(queue: Queue[IO, (Query, Source)]) = {
        val enqueue = for {
            queries <- queryRepository.queries(
              List(New.toString, WaitingRetry.toString)
            )
            _ <- NonEmptyList
                .fromList(queries)
                .map { nonEmptyQueries =>
                    nonEmptyQueries.map { case (q, _) =>
                        q.id
                    }
                }
                .map { qIds => queryRepository.update(Enqueued, qIds) }
                .getOrElse(IO.pure(List()))

            enqueueQueries <-
                if (queries.isEmpty)
                    info"Nothing to enqueue, queue is empty"
                else
                    queries.traverse_(q => queue.offer(q))

        } yield (enqueueQueries)
        enqueue >> IO.sleep(20.seconds)
    }

    def handleQueries(queue: Queue[IO, (Query, Source)]): IO[List[Unit]] = {
        queue.tryTakeN(Some(4)).flatMap { extracted =>
            extracted.parTraverse {
                case (q, s) => {
                    (for {
                        _ <- queryRepository.update(Running, q.id)
                        metricData <- IO.blocking {
                            localSparkMetricCalcService
                                .calc(q.toProps, s.toProps)
                        }
                        m <- metricRepository.insertOne(
                          q.id,
                          metricData
                        )
                        _ <- IO.println("Metric id: ", m)
                        _ <- queryRepository.update(
                          WaitingRetry,
                          retries = 0,
                          q.id
                        )
                    } yield ()).handleErrorWith { e =>
                        for {
                            _ <-
                                if (q.retries > MaxRetries) {
                                    queryRepository.update(
                                      Failed,
                                      q.id
                                    )
                                } else {
                                    queryRepository.update(
                                      state   = WaitingRetry,
                                      retries = q.retries + 1,
                                      id      = q.id
                                    )
                                }
                            _ <- IO.println("Raised:", e)
                        } yield ()
                    }
                }
            }
        }
    }
}
