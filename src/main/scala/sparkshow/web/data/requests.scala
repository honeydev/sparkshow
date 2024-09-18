package sparkshow.db.web.data

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class LoginRequest(
    username: String,
    password: String
)

object LoginRequest {

    def decoder: EntityDecoder[IO, LoginRequest] = jsonOf[IO, LoginRequest]
}

case class QueryRequest(sql: String)

object QueryRequest {
    implicit val decoder: EntityDecoder[IO, QueryRequest] =
        jsonOf[IO, QueryRequest]
}

