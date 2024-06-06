package sparkshow.web.routes

import cats.effect._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.db.web.data.LoginRequest
import sparkshow.service.AuthService

class AuthRoutes(authService: AuthService) {
    private implicit val loginReqDecoder: EntityDecoder[IO, LoginRequest] =
        LoginRequest.decoder
    val routes: HttpRoutes[IO] = HttpRoutes
        .of[IO] { case req @ POST -> Root / "login" =>
            req
                .as[LoginRequest]
                .flatMap(loginRequest => {
                    Ok(authService.authenticate(loginRequest))
                })
        }
}
