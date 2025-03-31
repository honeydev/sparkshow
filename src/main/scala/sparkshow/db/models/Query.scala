package sparkshow.db.models

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

@ConfiguredJsonCodec
case class Query(
    id: Long,
    userId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    sourcePath: String,
    retries: Int = 0
)

object Query {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val queryEncoder: Encoder[Query] = deriveEncoder[Query]
}

sealed trait Function
case object Sum extends Function {
  override def toString = "sum"
}
case object Count extends Function {
  override def toString = "count"
}
object Function {

  implicit val decoder: Decoder[Function] = Decoder.decodeString.emap {
    case "sum" => Right(Sum)
    case "count" => Right(Sum)
    case unknownFunction => Left(s"Unrecognised aggregate function $unknownFunction")
  }

  implicit val encoder: Encoder[Function] = Encoder.encodeString.contramap(_.toString)
}

case class Aggregate(column: String, function: Function)

object Aggregate {
    implicit val decoder: Decoder[Aggregate] = deriveDecoder[Aggregate]
    implicit val encoder: Encoder[Aggregate] = deriveEncoder[Aggregate]
}

sealed trait QueryState

object QueryState {
    def `new` = NEW.toString
}

object NEW extends QueryState {
    override def toString: String = "new"
}
object RUNNING extends QueryState
object FINISHED extends QueryState
object FAILED extends QueryState
