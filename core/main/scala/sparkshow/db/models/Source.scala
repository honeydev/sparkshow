package sparkshow.db.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sparkshow.data.Column
import sparkshow.db.models.Source.Schema
import sparkshow.services.SourceProperties

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
    def toProps =
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

    implicit val decoder = deriveDecoder[Source]
    implicit val encoder = deriveEncoder[Source]
}
