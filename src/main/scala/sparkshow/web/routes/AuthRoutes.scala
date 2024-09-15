package sparkshow.web.routes

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.conf.AppConf
import sparkshow.db.web.data.InvalidResponse
import sparkshow.db.web.data.LoginRequest
import sparkshow.db.web.data.LoginResponse
import sparkshow.service.AuthService
import sparkshow.utils.AuthUtils

class AuthRoutes(authService: AuthService, conf: AppConf) {
    private implicit val loginReqDecoder: EntityDecoder[IO, LoginRequest] =
        LoginRequest.decoder
    val routes: HttpRoutes[IO] = HttpRoutes
        .of[IO] { case req @ POST -> Root / "login" =>
            req
                .as[LoginRequest]
                .flatMap(loginRequest => {
                    authService.authenticate(loginRequest) flatMap { u =>
                        u match {
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
                    }
                })
        }
}
