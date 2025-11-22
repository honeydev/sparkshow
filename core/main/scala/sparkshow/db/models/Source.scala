package sparkshow.db.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sparkshow.db.models.Source.Schema
import sparkshow.services.SourceProperties
import io.circe.{Decoder, Encoder, HCursor, Json}
import sparkshow.data.{BaseColumn, Type, NumericT, StringT}

import java.time.Instant

case class Source (
    id: Long,
    createdAt: Instant,
    updatedAt: Instant,
    path: String,
    name: String,
    header: Boolean,
    delimiter: Option[String],
    schema: Schema
) {
    def toProps: SourceProperties =
        SourceProperties(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            path = path,
            name = name,
            header = header,
            delimiter = delimiter,
            schema = schema
        )
}

object Source {

    type Schema = List[Column]

    implicit val decoder: Decoder[Source] = deriveDecoder[Source]
    implicit val encoder: Encoder.AsObject[Source] = deriveEncoder[Source]
}

case class Column(override val name: String, override val `type`: Type) extends BaseColumn(name = name, `type` = `type`)
object Column {

    implicit val decoder: Decoder[Column] = new Decoder[Column] {
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

    implicit val encoder: Encoder[Column] = new Encoder[Column] {
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
