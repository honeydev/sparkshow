package sparkshow.db.models

import java.sql.Timestamp
import sparkshow.data.MetricValue
import sparkshow.db.models.Metric.Values

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
