package sparkshow.db.models

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

    case class Value(
        label: String,
        name: String,
        value: Long
    )

    type Values = List[Value]
}
