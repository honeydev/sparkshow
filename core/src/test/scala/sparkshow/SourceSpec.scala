package sparkshow

import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.literal.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits.*
import org.typelevel.ci.CIStringSyntax
import sparkshow.conf.AppConf
import sparkshow.data.StringT
import sparkshow.services.UserService
import sparkshow.utils.AuthUtils
import sparkshow.web.data.CreateSourceResponse
import sparkshow.web.routes.RoutesFacade

class SourceSpec extends BaseIntegrationSpec {
    private implicit val sourceDecoder: Decoder[CreateSourceResponse] =
        deriveDecoder[CreateSourceResponse]
    private implicit val sourceIODecoder
        : EntityDecoder[IO, CreateSourceResponse] =
        jsonOf[IO, CreateSourceResponse]

    "Test create source" in {
        (
            testWebApp: TestWebApp,
            routes: RoutesFacade,
            userService: UserService,
            conf: AppConf
        ) =>
            {
                for {
                    user <- userService.createUser("test", "test", "test")
                    request = {
                        val token = AuthUtils.encodeToken(user, conf.jwt.secret)
                        Request[IO](
                          method = Method.POST,
                          uri    = uri"/source",
                          headers = Headers(
                            Header.Raw(
                              name  = ci"Authorization",
                              value = s"Bearer $token"
                            )
                          )
                        ).withEntity(
                          json"""{
                              "name": "test",
                              "path": "abc",
                              "header": true,
                              "delimiter": ";",
                              "schema": [{"name": "username", "type": "string"}]
                              }"""
                        )
                    }
                    response <- testWebApp.routes.run(request)
                    body     <- response.as[CreateSourceResponse]
                    schema    = body.schema.head
                    _        <- assertIO(body.name === "test")
                    _        <- assertIO(body.path === "abc")
                    _        <- assertIO(body.header == true)
                    _        <- assertIO(body.delimiter == Some(";"))
                    _        <- assertIO(schema.name === "username")
                    _        <- assertIO(schema.`type` === StringT)
                } yield ()
            }
    }
}
