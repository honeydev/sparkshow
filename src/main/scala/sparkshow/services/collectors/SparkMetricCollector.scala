package sparkshow.services.collectors

import cats.effect.IO
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{
    LongType,
    StringType,
    StructField,
    StructType
}
import sparkshow.db.models._
import sparkshow.db.repositories.QueryRepository

class SparkMetricCollector(queryRepository: QueryRepository, sparkSession: SparkSession) extends MetricCollector[IO] {

    def collect(query: Query, source: Source) : IO[Unit] = {
        val calculate = IO.blocking {
            val schema =
                StructType(source.schema.map { col =>
                    val sparkT = col.`type` match {
                        case StringT  => StringType
                        case NumericT => LongType
                    }
                    StructField(col.name, sparkT)
                })

            val df = {
                val dfReader = sparkSession.read
                    .option("header", s"${source.header}")

                source
                    .delimiter
                    .map(dfReader.option("delimiter", _))
                    .getOrElse(dfReader)
                    .schema(schema)
                    .csv(source.path)
            }

            val grouped =
                df.groupBy(query.grouped.map(col): _*)

            val result = query.aggregate.function match {
                case Sum =>
                    grouped.sum(query.aggregate.column)
                case Count =>
                    grouped.agg(
                        count(col(query.aggregate.column))
                    )
            }
            // TODO: write as metric in separate table
            result.show()
        }

        val res = for {
            _ <- queryRepository.update(Running, query.id)
            _ <- calculate
            _ <- queryRepository.update(
                WaitingRetry,
                retries = 0,
                query.id
            )
        } yield ()

        res.handleErrorWith { e =>
            for {
                _ <-
                    if (query.retries > 3) {
                        queryRepository.update(
                            Failed,
                            query.id
                        )
                    } else {
                        queryRepository.update(
                            state   = WaitingRetry,
                            retries = query.retries + 1,
                            id      = query.id
                        )
                    }
                _ <- IO.println("Raised:", e)
            } yield ()
        }
    }
}
