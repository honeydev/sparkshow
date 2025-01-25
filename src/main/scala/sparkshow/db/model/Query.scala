package sparkshow.db.model

case class Query(
    id: Long,
    userId: Long,
    query: String,
    state: String
)

object Query {}

trait QueryState {
    override def toString = {
        "invalid"
    }
}

object QueryState {
    def `new` = NEW
}

object NEW extends QueryState {
    override def toString: String = "NEW"
}
object RUNNING extends QueryState
object FINISHED extends QueryState
object FAILED extends QueryState
