package sparkshow.db.models

import sparkshow.db.models.Metric.Values

case class Metric(
    id: Long,
    queryId: Long,
    values: Values,
    created_at: String,
    updated_at: String
)

object Metric {

    case class Value(
        label: String,
        name: String,
        value: Long
    )

    type Values = List[Value]
}
