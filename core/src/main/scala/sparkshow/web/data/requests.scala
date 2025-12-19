package sparkshow.web.data

import cats.data.EitherT
import cats.effect.IO
import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser._
import org.http4s.DecodeFailure
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.HttpVersion
import org.http4s.Media
import org.http4s.MediaType
import org.http4s.Response
import org.http4s.Status
import org.http4s.circe._
import sparkshow.data.Aggregate
import sparkshow.db.models.Column

given Configuration =
    Configuration.default.withDiscriminator("type").withSnakeCaseMemberNames

case class LoginRequestBody(
    username: String,
    password: String
)

object LoginRequestBody {

    def decoder: EntityDecoder[IO, LoginRequestBody] =
        jsonOf[IO, LoginRequestBody]
}

case class QueryRequestBody(
    sourceId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate
) derives ConfiguredCodec

object QueryRequestBody {

    import sparkshow.db.models.Aggregate.{decoder, encoder}
    implicit val decoder: Decoder[QueryRequestBody] =
        deriveDecoder[QueryRequestBody]

    implicit val entityDecoder: EntityDecoder[IO, QueryRequestBody] =
        EntityDecoder.decodeBy[IO, QueryRequestBody](
          MediaType.application.json
        ) { (media: Media[IO]) =>
            val queryRequestBody = media.as[String].map { rawJson =>
                decode[QueryRequestBody](rawJson)
            }
            EitherT[IO, io.circe.Error, QueryRequestBody](queryRequestBody)
                .leftMap { v =>
                    new DecodeFailure {

                        override def message: String = v.getMessage

                        override def cause: Option[Throwable] = Some(v.getCause)

                        override def toHttpResponse[F[_]](
                            httpVersion: HttpVersion
                        ): Response[F] =
                            Response(Status.BadRequest, httpVersion)
                                .withEntity("Json parse error")(
                                  EntityEncoder.stringEncoder[F]
                                )
                    }
                }
        }
}

case class SourceRequestBody(
    name: String,
    path: String,
    header: Boolean,
    delimiter: Option[String],
    schema: List[Column]
)

object SourceRequestBody {

    implicit val decoder: Decoder[SourceRequestBody] =
        deriveDecoder[SourceRequestBody]

    implicit val entityDecoder: EntityDecoder[IO, SourceRequestBody] =
        EntityDecoder.decodeBy[IO, SourceRequestBody](
          MediaType.application.json
        ) { (media: Media[IO]) =>
            val sourceRequestBody = media.as[String].map { rawJson =>
                for {
                    parsedJson <- parse(rawJson)
                    entity     <- decoder.decodeJson(parsedJson)
                } yield entity
            }

            EitherT[IO, io.circe.Error, SourceRequestBody](sourceRequestBody)
                .leftMap { v =>
                    new DecodeFailure {

                        override def message: String = v.getMessage

                        override def cause: Option[Throwable] = Some(v.getCause)

                        override def toHttpResponse[F[_]](
                            httpVersion: HttpVersion
                        ): Response[F] =
                            Response(Status.BadRequest, httpVersion)
                                .withEntity("Json parse error")(
                                  EntityEncoder.stringEncoder[F]
                                )
                    }
                }
        }
}
