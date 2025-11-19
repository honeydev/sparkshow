package sparkshow
import cats.effect.IO
import cats.implicits._
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Method, Request, Status}
import sparkshow.services.UserService

class LoginSpec extends BaseIntegrationSpec {

    case class ResponseUser(id: Long, username: String, email: String)
    case class LoginResponseTest(
        status: String,
        user: ResponseUser,
        token: String
    )

    private implicit val decoder: EntityDecoder[IO, LoginResponseTest] =
        jsonOf[IO, LoginResponseTest]

    "Test login happy path" in {
        (
            testWebApp: TestWebApp,
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
                    res  <- testWebApp.routes.run(req)
                    _    <- IO.println(res.as[String])
                    body <- res.as[LoginResponseTest]
                    _    <- assertIO(res.status == Status.Ok)
                    _    <- assertIO(body.user.username == "test")
                    _    <- assertIO(body.user.email == "test")
                    _    <- assertIO(body.token.nonEmpty)
                } yield ()
            }
    }
}
