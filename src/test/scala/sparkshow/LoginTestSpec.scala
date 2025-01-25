package sparkshow

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s.{EntityDecoder, Method, Request, Status}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import sparkshow.db.web.data.LoginResponse
import sparkshow.service.UserService
import sparkshow.web.routes.RoutesFacade

class LoginTestSpec extends BaseIntegrationSpec {

    private implicit val decoder: EntityDecoder[IO, LoginResponse] =
        jsonOf[IO, LoginResponse]

    "Test login happy path" in {
        (
            routes: RoutesFacade,
            userService: UserService
        ) =>
            {
                val req = Request[IO](
                  method = Method.POST,
                  uri    = uri"/login"
                ).withEntity(
                  json"""{"username": "test", "password": "test"}"""
                )

                for {
                    _    <- userService.createUser("test", "test", "test")
                    res  <- routes.build.orNotFound.run(req)
                    body <- res.as[LoginResponse]
                    _    <- assertIO(res.status == Status.Ok)
                    _    <- assertIO(body.user.username == "test")
                    _    <- assertIO(body.user.passwordHash.nonEmpty)
                    _    <- assertIO(body.user.email == Some("test"))
                    _    <- assertIO(body.token.nonEmpty)
                } yield ()
            }
    }
}
