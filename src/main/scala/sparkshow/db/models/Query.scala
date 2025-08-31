package sparkshow.db.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import java.sql.Timestamp

case class Query(
    id: Long,
    userId: Long,
    sourceId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0,
    createdAt: Timestamp,
    updatedAt: Timestamp
)

sealed trait Function
case object Sum extends Function {
    override def toString = "sum"
}
case object Count extends Function {
    override def toString = "count"
}
object Function {

    implicit val decoder: Decoder[Function] = Decoder.decodeString.emap {
        case "sum"   => Right(Sum)
        case "count" => Right(Count)
        case unknownFunction =>
            Left(s"Unrecognised aggregate function $unknownFunction")
    }

    implicit val encoder: Encoder[Function] =
        Encoder.encodeString.contramap(_.toString)
}

case class Aggregate(column: String, function: Function)

object Aggregate {
    implicit val decoder: Decoder[Aggregate] = deriveDecoder[Aggregate]
    implicit val encoder: Encoder[Aggregate] = deriveEncoder[Aggregate]
}

sealed trait QueryState

object QueryState {
    def `new`                  = New.toString
    def `failed`: String       = Failed.toString
    def `waitingRetry`: String = WaitingRetry.toString
    def `enqueued`: String     = Enqueued.toString
}

object New extends QueryState {
    override def toString: String = "new"
}

object Failed extends QueryState {
    override def toString: String = "failed"
}

object WaitingRetry extends QueryState {
    override def toString: String = "waiting_retry"
}

object Running extends QueryState {
    override def toString: String = "running"
}

object Enqueued extends QueryState {

    override def toString: String = "enqueued"

}
