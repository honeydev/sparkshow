package sparkshow.data
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

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
