package sparkshow

import cats.effect.IO
import io.circe.literal.JsonStringContext
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import sparkshow.service.UserService
import sparkshow.web.routes.RoutesFacade

class LoginTestSpec extends BaseIntegrationSpec {

    "Test login happy path" in {
        (
            routes: RoutesFacade,
            userService: UserService,
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
                    body <- res.as[String]
                    _    <- assertIO(res.status == Status.Ok)
                    _    <- assertIO(body == "")
                } yield ()
            }
    }
}
