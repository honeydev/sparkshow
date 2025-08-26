package sparkshow.services

import cats.effect._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import sparkshow.db.models.Count
import sparkshow.db.models.Metric
import sparkshow.db.models.NumericT
import sparkshow.db.models.Query
import sparkshow.db.models.Source
import sparkshow.db.models.StringT
import sparkshow.db.models.Sum
import sparkshow.db.repositories.QueryRepository

class LocalSparkMetricCalcService(
    private val sparkSession: SparkSession,
    private val queryRepository: QueryRepository
) {
    def calc(query: Query, source: Source): IO[List[Metric.Value]] =
        IO.blocking {
            val schema =
                StructType(source.schema.map { col =>
                    val sparkT = col.`type` match {
                        case StringT  => StringType
                        case NumericT => LongType
                    }
                    StructField(col.name, sparkT)
                })

            val df = sparkSession.read
                .option("header", s"${source.header}")
                .option("delimiter", source.delimiter.getOrElse(""))
                .schema(schema)
                // TODO impl diff file sources json, parquet, etc.
                .csv(source.path)

            val grouped =
                df.groupBy(query.grouped.map(col): _*)

            val aggregated =
                (query.aggregate.function match {
                    case Sum =>
                        grouped.agg(
                          sum(query.aggregate.column)
                              .alias(query.aggregate.column)
                        )
                    case Count =>
                        grouped.agg(
                          count(
                            col(query.aggregate.column)
                                .alias(
                                  query.aggregate.column
                                )
                          )
                        )
                })

            aggregated
                .collect()
                .map(r =>
                    Metric.Value(
                      label = query.grouped.mkString(", "),
                      name = query.grouped
                          .map(r.getAs[String](_))
                          .mkString(", "),
                      value = r.getAs[Long](
                        query.aggregate.column
                      )
                    )
                )
                .toList
        }
}
