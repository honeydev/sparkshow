package sparkshow.web.routes
import cats.effect._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import sparkshow.web.data.QueryRequest

class QueryRoutes {

    private implicit val queryReqDecoder: EntityDecoder[IO, QueryRequest] =
        QueryRequest.decoder
    val routes = createQuery
    private val createQuery = HttpRoutes
        .of[IO] { case req @ POST -> Root / "query" =>
            req
                .as[QueryRequest]
                .flatMap(r => {
                    Ok("Create query stub")
                })
        }
}
