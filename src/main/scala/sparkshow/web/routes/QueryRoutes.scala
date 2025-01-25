package sparkshow.web.routes

import cats.effect._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.model.User
import sparkshow.db.web.data.{CreateQueryResponse, QueryRequestBody}
import sparkshow.service.{QueryService, UserService}

class QueryRoutes(
    val userService: UserService,
    val queryService: QueryService,
    val conf: AppConf
) {

    private implicit val requestDecoder: EntityDecoder[IO, QueryRequestBody] =
        QueryRequestBody.decoder

    val routes = AuthedRoutes
        .of[User, IO] { case authedRequest @ POST -> Root / "query" as user =>
            authedRequest.req
                .as[QueryRequestBody]
                .flatMap(request =>
                    for {
                        query    <- queryService.createQuery(request, user)
                        response <- Ok(CreateQueryResponse(query).asJson)
                    } yield response
                )
        }
}
