package sparkshow.services

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import sparkshow.data.{Aggregate, BaseColumn, Count, MetricValue, NumericT, StringT, Sum}

import java.time.Instant

case class QueryProperties(
                              id: Long,
                              userId: Long,
                              sourceId: Long,
                              createdAt: Instant,
                              updatedAt: Instant,
                              columns: List[String],
                              grouped: List[String],
                              aggregate: Aggregate,
                              state: String,
                              retries: Int = 0
                          )

case class SourceProperties(
                               id: Long,
                               createdAt: Instant,
                               updatedAt: Instant,
                               path: String,
                               name: String,
                               header: Boolean,
                               delimiter: Option[String],
                               schema: List[BaseColumn]
)

class LocalSparkMetricCalcService {

    def calc(queryProperties: QueryProperties, sourceProperties: SourceProperties): List[MetricValue] = {
            val sparkSession = SparkSession.builder
                        .appName("sparkshow run")
                        .master("local[*]")
                        .getOrCreate()
            val schema =
                StructType(sourceProperties.schema.map { col =>
                    val sparkT = col.`type` match {
                        case StringT  => StringType
                        case NumericT => LongType
                    }
                    StructField(col.name, sparkT)
                })

            val df = sparkSession.read
                .option("header", s"${sourceProperties.header}")
                .option("delimiter", sourceProperties.delimiter.getOrElse(""))
                .schema(schema)
                // TODO impl diff file sources json, parquet, etc.
                .csv(sourceProperties.path)

            val grouped =
                df.groupBy(queryProperties.grouped.map(col): _*)

            val aggregated =
                (queryProperties.aggregate.function match {
                    case Sum =>
                        grouped.agg(
                          sum(queryProperties.aggregate.column)
                              .alias(queryProperties.aggregate.column)
                        )
                    case Count =>
                        grouped.agg(
                          count(
                            col(queryProperties.aggregate.column)
                                .alias(
                                  queryProperties.aggregate.column
                                )
                          )
                        )
                })

            aggregated
                .collect()
                .map(r =>
                    MetricValue(
                      label = queryProperties.grouped.mkString(", "),
                      name = queryProperties.grouped
                          .map(r.getAs[String](_))
                          .mkString(", "),
                      value = r.getAs[Long](
                        queryProperties.aggregate.column
                      )
                    )
                )
                .toList
    }
}
