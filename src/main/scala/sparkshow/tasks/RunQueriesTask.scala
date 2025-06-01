package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import doobie.enumerated.JdbcType.Struct
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{
    LongType,
    StringType,
    StructField,
    StructType
}
import org.scalactic.anyvals.NonEmptyList
import sparkshow.db.models.{
    Count,
    Failed,
    New,
    NumericT,
    Query,
    QueryState,
    Running,
    Source,
    StringT,
    Sum,
    WaitingRetry
}
import sparkshow.db.repositories.QueryRepository

import scala.concurrent.duration._

class RunQueriesTask(
    val queryRepository: QueryRepository,
    val sparkSession: SparkSession
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
                            case (q, s) => {
                                val calculate = IO.blocking {
                                    val schema =
                                        StructType(s.schema.map { col =>
                                            val sparkT = col.`type` match {
                                                case StringT  => StringType
                                                case NumericT => LongType
                                            }
                                            StructField(col.name, sparkT)
                                        })

                                    val df = sparkSession.read
                                        .option("header", "true")
                                        .option("delimiter", ";")
                                        .schema(schema)
                                        .csv(s.path)

                                    val grouped =
                                        df.groupBy(q.grouped.map(col): _*)

                                    val result = q.aggregate.function match {
                                        case Sum =>
                                            grouped.sum(q.aggregate.column)
                                        case Count =>
                                            grouped.agg(
                                              count(col(q.aggregate.column))
                                            )
                                    }
                                    // TODO: write as metric in separate table
                                    result.show()
                                }

                                val res = for {
                                    _ <- queryRepository.update(Running, q.id)
                                    _ <- calculate
                                    _ <- queryRepository.update(
                                      WaitingRetry,
                                      retries = 0,
                                      q.id
                                    )
                                } yield ()

                                res.handleErrorWith { e =>
                                    for {
                                        _ <-
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
