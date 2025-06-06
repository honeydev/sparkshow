package sparkshow.web.routes

import cats.effect.IO
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.{AuthedRoutes, EntityDecoder}
import sparkshow.conf.AppConf
import sparkshow.db.models.User
import sparkshow.services.SourceService
import sparkshow.web.data.CreateSourceResponse._
import sparkshow.web.data.{CreateSourceResponse, SourceRequestBody}

class SourceRoutes(val sourceService: SourceService, val conf: AppConf) {

    private implicit val requestDecoder: EntityDecoder[IO, SourceRequestBody] =
        SourceRequestBody.entityDecoder

    val routes = AuthedRoutes
        .of[User, IO] { case authedRequest @ POST -> Root / "source" as user =>
            authedRequest.req
                .as[SourceRequestBody]
                .flatMap(request =>
                    for {
                        source <- sourceService.createSource(request)
                        response <- Ok(
                          CreateSourceResponse.fromSource(source).asJson
                        )
                    } yield response
                )
        }
}
