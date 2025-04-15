package sparkshow.web.routes

import cats.effect.IO
import org.http4s.{AuthedRoutes, EntityDecoder}
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.models.User
import sparkshow.services.QueryService
import sparkshow.web.data.{CreateQueryResponse, QueryRequestBody, SourceRequestBody}

class SourceRoutes(
                      val queryService: QueryService,
                      val conf: AppConf
                  ) {
    private implicit val requestDecoder: EntityDecoder[IO, SourceRequestBody] =
        SourceRequestBody.entityDecoder

    val routes = AuthedRoutes
        .of[User, IO] { case authedRequest @ POST -> Root / "source" as user =>
            authedRequest.req
                .as[SourceRequestBody]
                .flatMap(request =>
                    for {
//                        query    <- queryService.createQuery(request, user)
                        response <- Ok("""{"success": true}""")
                    } yield response
                )
        }
}
