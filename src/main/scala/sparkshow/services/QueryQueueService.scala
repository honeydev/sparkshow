package sparkshow.services

import scala.concurrent.duration._

import cats.effect._
import cats.effect.std.Queue
import cats.syntax.all._
import sparkshow.db.models.Enqueued
import sparkshow.db.models.Failed
import sparkshow.db.models.New
import sparkshow.db.models.Query
import sparkshow.db.models.Running
import sparkshow.db.models.Source
import sparkshow.db.models.WaitingRetry
import sparkshow.db.repositories.MetricRepository
import sparkshow.db.repositories.QueryRepository

class QueryQueueService(
    private val queryRepository: QueryRepository,
    private val metricRepository: MetricRepository,
    private val localSparkMetricCalcService: LocalSparkMetricCalcService
) {

    def produceQueries(queue: Queue[IO, (Query, Source)]) = {
        val enqueueQueries = for {
            queries <- queryRepository.queries(
              List(New.toString, WaitingRetry.toString)
            )
            _ <- queryRepository.update(
              Enqueued,
              queries.map { case (q, _) => q.id }
            )
            _  <- IO.println(s"Enqueued: $queries")
            zs <- queries.traverse_(q => queue.offer(q))
        } yield (zs)

        enqueueQueries >> IO.sleep(20.second)
    }

    def handleQueries(queue: Queue[IO, (Query, Source)]): IO[List[Unit]] = {
        queue.tryTakeN(Some(4)).flatMap { extracted =>
            extracted.parTraverse {
                case (q, s) => {

                    val metricValues = localSparkMetricCalcService.calc(q, s)

                    (for {
                        _          <- queryRepository.update(Running, q.id)
                        metricData <- metricValues
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
                                // TODO Move to const
                                if (q.retries > 3) {
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
