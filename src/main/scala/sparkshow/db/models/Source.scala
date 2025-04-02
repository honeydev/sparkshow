package sparkshow.db.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sparkshow.db.models.Source.Schema
import io.circe.{Decoder, Encoder}

sealed trait Type
case object NumericT extends Type
case object StringT extends Type

case class Column(name: String, `type`: Type)
case class Source(id: Long, name: String,  path: String, schema: Schema)
object Source {
    type Schema = List[Column]

    implicit val decoder = deriveDecoder[Column]
    implicit val encoder = deriveEncoder[Column]
}
