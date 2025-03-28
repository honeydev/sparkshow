package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import sparkshow.db.models.{Count, Query, Sum}
import sparkshow.db.repositories.QueryRepository

import scala.concurrent.duration._

class RunQueriesTask(val queryRepository: QueryRepository, val sparkSession: SparkSession) {

    def run: IO[Unit] = {
        def loop(queue: IO[Queue[IO, Query]]): IO[Unit] = {
            val nextQ = {
                for {
                    q <- queue
                    queries <- queryRepository.newQueries
                    _ <- q.tryOfferN(queries)
                    extracted <- q.tryTakeN(Some(2))
                    _ <- IO.println(extracted)
                    updated <- {
                        extracted.parTraverse(q => {
                                // TODO: implement
                                IO.blocking {
                                    val df = sparkSession
                                        .read
                                        // TODO: configure header
                                        .option("header", "true")
                                        .csv(q.sourcePath)
                                        .groupBy(q.grouped.map(col) :_*)

                                    q.aggregate.function match {
                                        case Sum => df.sum(q.aggregate.column)
                                        case Count => df.agg(count(col(q.aggregate.column)))
                                    }
                                }.handleErrorWith { e =>
                                  IO.println("Raised error", e)
                                }
                            }
                        )
                    }
                    _ <- IO.println(updated)
                } yield (q)
            }
            nextQ >> IO.println("DELAY END") >> IO.defer { loop(nextQ) }.delayBy(300.seconds)
        }
        loop(Queue.bounded[IO, Query](50))
    }
}
