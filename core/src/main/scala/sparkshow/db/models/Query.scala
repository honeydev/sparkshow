package sparkshow.db.models

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import java.time.Instant
import sparkshow.data.Aggregate
import sparkshow.data.Count
import sparkshow.data.Function
import sparkshow.data.Sum
import sparkshow.services.QueryProperties

case class Query(
    id: Long,
    userId: Long,
    sourceId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0,
    createdAt: Instant,
    updatedAt: Instant
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

    given decoder: Decoder[Aggregate] = deriveDecoder[Aggregate]
    given encoder: Encoder[Aggregate] = deriveEncoder[Aggregate]
}
