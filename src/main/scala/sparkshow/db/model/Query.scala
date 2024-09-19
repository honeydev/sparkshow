package sparkshow.db.model

object QueryState extends Enumeration {
    type QueryState = Value
    val NEW, RUNNING, FINISHED, FAILED = Value
}

case class Query(
    id: Long,
    query: String,
    state: String
)

