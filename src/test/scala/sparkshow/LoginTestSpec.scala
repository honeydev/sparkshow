package sparkshow
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s.{EntityDecoder, Method, Request, Status}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import sparkshow.services.UserService
import sparkshow.web.data.LoginResponse
import sparkshow.web.routes.RoutesFacade
import io.circe.generic.semiauto.deriveDecoder

class LoginTestSpec extends BaseIntegrationSpec {

  case class ResponseUser(id: Long, username: String, email: String)
  case class LoginResponseTest(status: String, user: ResponseUser, token: String)


    private implicit val decoder: EntityDecoder[IO, LoginResponseTest] =
        jsonOf[IO, LoginResponseTest]

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
                    body <- res.as[LoginResponseTest]
                    _    <- assertIO(res.status == Status.Ok)
                    _    <- assertIO(body.user.username == "test")
                    _    <- assertIO(body.user.email == "test")
                    _    <- assertIO(body.token.nonEmpty)
                } yield ()
            }
    }
}
