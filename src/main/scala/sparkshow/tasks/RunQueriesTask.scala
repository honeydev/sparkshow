package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import sparkshow.db.models._
import sparkshow.db.repositories.QueryRepository
import sparkshow.services.collectors.SparkMetricCollector

import scala.concurrent.duration._

class RunQueriesTask(
    val queryRepository: QueryRepository,
    val sparkSession: SparkSession,
    val sparkMetricCollector: SparkMetricCollector
) {

    def run: IO[Unit] = {
        def loop(queue: IO[Queue[IO, (Query, Source)]]): IO[Unit] = {
            val nextQ = {
                for {
                    q <- queue
                    queries <- queryRepository.queries(
                      List(New.toString, WaitingRetry.toString)
                    )
                    _         <- q.tryOfferN(queries)
                    extracted <- q.tryTakeN(Some(10))
                    _         <- IO.println(extracted)
                    updated <- {
                        extracted.parTraverse {
                            case (q, s) => sparkMetricCollector.collect(q, s)
                        }
                    }
                    _ <- IO.println(updated)
                } yield (q)
            }
            nextQ >> IO.println("DELAY END") >> IO
                .defer { loop(nextQ) }
                .delayBy(300.seconds)
        }
        loop(Queue.bounded[IO, (Query, Source)](50))
    }
}
