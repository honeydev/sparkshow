package sparkshow.db.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sparkshow.data.Aggregate
import sparkshow.services.QueryProperties
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sparkshow.data.{Function, Sum, Count}

import java.time.Instant

case class Query(
    id: Long,
    userId: Long,
    sourceId: Long,
    createdAt: Instant,
    updatedAt: Instant,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0
) {
    def toProps =
        QueryProperties(
            this.id,
            this.userId,
            this.sourceId,
            this.createdAt,
            this.updatedAt,
            this.columns,
            this.grouped,
            this.aggregate,
            this.state,
            this.retries
        )
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

object Aggregate {
    import Function.{decoder, encoder}
    
    implicit val decoder: Decoder[Aggregate] = deriveDecoder[Aggregate]
    implicit val encoder: Encoder[Aggregate] = deriveEncoder[Aggregate]
}
