package sparkshow.db.models

import sparkshow.data.MetricValue
import sparkshow.db.models.Metric.Values

import java.sql.Timestamp

case class Metric(
                     id: Long,
                     queryId: Long,
                     values: Values,
                     createdAt: Timestamp,
                     updatedAt: Timestamp
                 )

object Metric {

    type Values = List[MetricValue]
}
