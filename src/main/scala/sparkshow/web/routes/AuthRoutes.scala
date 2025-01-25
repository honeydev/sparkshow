package sparkshow.web.routes

import cats.effect._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.web.data.{InvalidResponse, LoginRequestBody, LoginResponse}
import sparkshow.service.AuthService
import sparkshow.utils.AuthUtils

class AuthRoutes(authService: AuthService, conf: AppConf) {

    private implicit val loginReqDecoder: EntityDecoder[IO, LoginRequestBody] =
        LoginRequestBody.decoder

    val routes: HttpRoutes[IO] = HttpRoutes
        .of[IO] { case req @ POST -> Root / "login" =>
            req
                .as[LoginRequestBody]
                .flatMap(loginRequest => {
                    authService.authenticate(loginRequest) flatMap {
                        case None =>
                            NotFound(
                              InvalidResponse(message = "User not found")
                            )
                        case Some(u) =>
                            Ok(
                              LoginResponse(
                                user = u,
                                token = AuthUtils
                                    .encodeToken(u, conf.jwt.secret)
                              ).asJson
                            )
                    }
                })
        }
}
