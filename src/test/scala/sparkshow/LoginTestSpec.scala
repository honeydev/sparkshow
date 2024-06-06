package sparkshow

import cats.effect.IO
import io.circe.literal.JsonStringContext
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.implicits._
import sparkshow.web.routes.RoutesFacade

class LoginTestSpec extends BaseIntegrationSpec {

    "Test login happy path" in { (routes: RoutesFacade) =>
        {
            val req = Request[IO](
              method = Method.POST,
              uri = uri"/login"
            ).withEntity(
              json"""{"username": "test", "password": "test"}"""
            )
            val response = routes.build.orNotFound.run(req)
            for {
                res <- response
                _ <- assertIO(res.status == Status.Ok)
            } yield ()
        }
    }
}
