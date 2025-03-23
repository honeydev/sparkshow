package sparkshow.db.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Query(
    id: Long,
    userId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
)

case class Aggregate(column: String, function: String)

object Aggregate {

    implicit val decoder: Decoder[Aggregate] = deriveDecoder[Aggregate]
    implicit val encoder: Encoder[Aggregate] = deriveEncoder[Aggregate]
}

trait QueryState

object QueryState {
    def `new` = NEW.toString
}

object NEW extends QueryState {
    override def toString: String = "new"
}
object RUNNING extends QueryState
object FINISHED extends QueryState
object FAILED extends QueryState
