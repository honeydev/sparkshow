package sparkshow

import cats.effect.IO
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.typelevel.ci.CIStringSyntax
import sparkshow.conf.AppConf
import sparkshow.db.models.{Column, QueryState, StringT, Sum}
import sparkshow.db.repositories.SourceRepository
import sparkshow.services.UserService
import sparkshow.utils.AuthUtils
import sparkshow.web.data.CreateQueryResponse
import sparkshow.web.routes.RoutesFacade

class QuerySpec extends BaseIntegrationSpec {

    private implicit val queryDecoder: EntityDecoder[IO, CreateQueryResponse] =
        jsonOf[IO, CreateQueryResponse]

    "Test create query" in {
        (
            routes: RoutesFacade,
            userService: UserService,
            sourceRepository: SourceRepository,
            conf: AppConf
        ) =>
            {
                for {
                    user <- userService.createUser("test", "test", "test")
                    source <- sourceRepository.insertOne(
                      name      = "test",
                      path      = "test",
                      header    = true,
                      delimiter = Some(";"),
                      schema = List(Column(name = "colname", `type` = StringT))
                    )
                    request = {
                        val token = AuthUtils.encodeToken(user, conf.jwt.secret)
                        Request[IO](
                          method = Method.POST,
                          uri    = uri"/query",
                          headers = Headers(
                            Header.Raw(
                              name  = ci"Authorization",
                              value = s"Bearer $token"
                            )
                          )
                        ).withEntity(
                          json"""{
                             "source_id": ${source.id},
                             "columns": ["username"],
                             "grouped": ["position"],
                             "aggregate": {"column": "salary", "function": "sum"}
                       }"""
                        )
                    }
                    response <- routes.build.orNotFound.run(request)
                    body     <- response.as[CreateQueryResponse]
                    _        <- assertIO(body.id > 0)
                    _        <- assertIO(body.state === QueryState.`new`)
                    _        <- assertIO(body.columns == List("username"))
                    _        <- assertIO(body.grouped == List("position"))
                    _        <- assertIO(body.aggregate.column == "salary")
                    _        <- assertIO(body.aggregate.function == Sum)
                } yield ()
            }
    }
}
