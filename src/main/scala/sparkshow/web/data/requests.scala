package sparkshow.web.data

import cats.data.EitherT
import cats.effect.IO
import io.circe.{Decoder, HCursor}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.{DecodeFailure, EntityDecoder, EntityEncoder, HttpVersion, Media, MediaType, Response, Status}
import org.http4s.circe._
import sparkshow.db.models.{Aggregate, Column, NumericT, StringT}

case class LoginRequestBody(
    username: String,
    password: String
)

object LoginRequestBody {

    def decoder: EntityDecoder[IO, LoginRequestBody] =
        jsonOf[IO, LoginRequestBody]
}

case class QueryRequestBody(
    resourceId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    source_path: String
)

object QueryRequestBody {
    implicit val decoder: EntityDecoder[IO, QueryRequestBody] =
        jsonOf[IO, QueryRequestBody]
}

case class SourceRequestBody(
    name: String,
    path: String,
    schema: List[Column]
)

object SourceRequestBody {
    import sparkshow.db.models.Column.decoder

    implicit val decoder = deriveDecoder[SourceRequestBody]

    implicit val entityDecoder = EntityDecoder.decodeBy[IO, SourceRequestBody](MediaType.application.json) {
        media: Media[IO] =>
            val sourceRequestBody = media.as[String].map { rawJson =>
                for {
                    parsedJson <- parse(rawJson)
                    entity <- decoder.decodeJson(parsedJson)
                } yield entity
            }

            EitherT[IO, io.circe.Error, SourceRequestBody](sourceRequestBody).leftMap {
                v =>
                    new DecodeFailure {

                        override def message: String = v.getMessage

                        override def cause: Option[Throwable] = Some(v.getCause)

                        override def toHttpResponse[F[_]](httpVersion: HttpVersion): Response[F] =
                            Response(Status.BadRequest, httpVersion)
                                .withEntity("Json parse error")(EntityEncoder.stringEncoder[F])
                    }
            }
    }
}
