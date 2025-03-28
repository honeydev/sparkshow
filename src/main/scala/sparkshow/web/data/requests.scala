package sparkshow.web.data

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import sparkshow.db.models.Aggregate

case class LoginRequestBody(
    username: String,
    password: String
)

object LoginRequestBody {

    def decoder: EntityDecoder[IO, LoginRequestBody] =
        jsonOf[IO, LoginRequestBody]
}

case class QueryRequestBody(
                            columns: List[String],
                            grouped: List[String],
                            aggregate: Aggregate,
                            source_path: String
                           )

object QueryRequestBody {
    implicit val decoder: EntityDecoder[IO, QueryRequestBody] =
        jsonOf[IO, QueryRequestBody]
}
