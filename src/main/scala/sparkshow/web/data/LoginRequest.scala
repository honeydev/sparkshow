package sparkshow.db.web.data

import cats.effect.IO
import org.http4s.circe.jsonOf
import io.circe.generic.auto._
import org.http4s.EntityDecoder

case class LoginRequest(
    username: String,
    password: String
)

object LoginRequest {

    def decoder: EntityDecoder[IO, LoginRequest] = jsonOf[IO, LoginRequest]
}
