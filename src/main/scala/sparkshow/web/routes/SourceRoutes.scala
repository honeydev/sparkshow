package sparkshow.web.routes

import cats.effect.IO
import org.http4s.{AuthedRoutes, EntityDecoder}
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.models.User
import sparkshow.services.{QueryService, SourceService}
import sparkshow.web.data.{CreateQueryResponse, QueryRequestBody, SourceRequestBody}

class SourceRoutes(
     val sourceService: SourceService,
     val conf: AppConf) {

    private implicit val requestDecoder: EntityDecoder[IO, SourceRequestBody] =
        SourceRequestBody.entityDecoder

    val routes = AuthedRoutes
        .of[User, IO] { case authedRequest @ POST -> Root / "source" as user =>
            authedRequest.req
                .as[SourceRequestBody]
                .flatMap(request =>
                    for {
                        source    <- sourceService.createSource(request)
                        response  <- Ok(s"""{"success": ${source}}""")
                    } yield response
                )
        }
}
