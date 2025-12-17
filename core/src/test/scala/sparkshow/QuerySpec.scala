package sparkshow
import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.*
import io.circe.literal.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits.*
import org.typelevel.ci.CIStringSyntax
import sparkshow.conf.AppConf
import sparkshow.data.{QueryState, StringT, Sum}
import sparkshow.db.models.Column
import sparkshow.db.repositories.SourceRepository
import sparkshow.services.UserService
import sparkshow.utils.AuthUtils
import sparkshow.web.data.CreateQueryResponse

class QuerySpec extends BaseIntegrationSpec {
    import sparkshow.db.models.Aggregate.decoder
    private implicit val queryDecoder: Decoder[CreateQueryResponse] =
        deriveDecoder[CreateQueryResponse]
    private implicit val queryIODecoder
        : EntityDecoder[IO, CreateQueryResponse] =
        jsonOf[IO, CreateQueryResponse]

    "Test create query" in {
        (
            testWebApp: TestWebApp,
            userService: UserService,
            sourceRepository: SourceRepository,
            conf: AppConf
        ) =>
            {
                val j = json"""{
                             "source_id": 1,
                             "columns": ["username"],
                             "grouped": ["position"],
                             "aggregate": {"column": "salary", "function": "sum"}
                       }"""
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
                          j
                        )
                    }
                    response <- testWebApp.routes.run(request)
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
