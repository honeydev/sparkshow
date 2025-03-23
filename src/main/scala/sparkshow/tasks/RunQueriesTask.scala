package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import org.apache.spark.sql.SparkSession
import sparkshow.db.models.Query
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
                                    val count = sparkSession
                                        .read
                                        .option("header", "true")
                                        .csv("src/main/resources/users.csv")
                                        .count()
                                    count
                                }
                            }
                        )
                    }
                    _ <- IO.println(updated)
                } yield (q)
            }
            nextQ >> IO.println("DELAY END") >> IO.defer { loop(nextQ) }.delayBy(30.seconds)
        }
        loop(Queue.bounded[IO, Query](50))
    }
}
