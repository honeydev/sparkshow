package sparkshow

import cats.effect.IO
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.implicits._
import sparkshow.service.UserService
import sparkshow.web.routes.RoutesFacade

class QueryIntegrationSpec extends BaseIntegrationSpec {

    "Test create request" in {
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
                    _   <- userService.createUser("test", "test", "test")
                    res <- routes.build.orNotFound.run(req)
//                    body <- res.as[LoginResponse]
//                    _ <- assertIO(res.status == Status.Ok)
//                    _ <- assertIO(body.username == "test")
//                    _ <- assertIO(body.passwordHash.nonEmpty)
//                    _ <- assertIO(body.email == "test")
                } yield ()
            }
    }
}
