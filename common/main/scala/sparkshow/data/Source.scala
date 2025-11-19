package sparkshow.data

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

sealed trait Type
case object NumericT extends Type
case object StringT extends Type

case class Column(name: String, `type`: Type)
object Column {

    implicit val decoder = new Decoder[Column] {
        final def apply(c: HCursor): Decoder.Result[Column] =
            for {
                name <- c.downField("name").as[String]
                _type <- c.downField("type").as[String].map(_.toLowerCase).map {
                    case "numeric" => NumericT
                    case "string"  => StringT
                }
            } yield {
                Column(name, _type)
            }
    }

    implicit val encoder = new Encoder[Column] {
        override def apply(a: Column): Json =
            Json.obj(
              (
                "name",
                Json.fromString(a.name)
              ),
              (
                "type",
                Json.fromString(
                  a.`type` match {
                      case NumericT => "numeric"
                      case StringT  => "string"
                  }
                )
              )
            )
    }
}
