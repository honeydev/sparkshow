package sparkshow.web.data

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class QueryRequest(sql: String)

object QueryRequest {
    def decoder: EntityDecoder[IO, QueryRequest] = jsonOf[IO, QueryRequest]
}
