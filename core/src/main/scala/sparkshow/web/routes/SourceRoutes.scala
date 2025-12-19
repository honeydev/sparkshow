package sparkshow.web.routes

import cats.effect.IO
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.EntityDecoder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.models.User
import sparkshow.services.SourceService
import sparkshow.web.data.CreateSourceResponse
import sparkshow.web.data.SourceRequestBody

class SourceRoutes(val sourceService: SourceService, val conf: AppConf) {
    import sparkshow.web.data.CreateSourceResponse.jsonEncoder

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
