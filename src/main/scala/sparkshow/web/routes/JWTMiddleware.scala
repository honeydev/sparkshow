package sparkshow.web.routes

import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect._
import io.circe.syntax.EncoderOps
import org.http4s.Credentials.Token
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import sparkshow.conf.AppConf
import sparkshow.db.models.User
import sparkshow.web.data.InvalidResponse
import sparkshow.services.UserService
import sparkshow.utils.AuthUtils

class JWTMiddleware(val userService: UserService, val conf: AppConf) {

    val mw = AuthMiddleware[IO, String, User](authUser, onFailure)

    private def authUser: Kleisli[IO, Request[IO], Either[String, User]] =
        Kleisli(request => {
            val targetUser = for {
                jwtPayload <- {
                    val payload = request.headers.get[Authorization] match {
                        case Some(Authorization(Token(_, token))) =>
                            AuthUtils.decodeToken(token, conf.jwt.secret)
                        case _ => Left("JWT token is not found")
                    }
                    EitherT.fromEither[IO](payload)
                }
                user <- userService.findUser(jwtPayload.id)
            } yield user

            targetUser.value
        })

    private def onFailure: AuthedRoutes[String, IO] =
        Kleisli(req =>
            OptionT.liftF(
              Forbidden(InvalidResponse(message = req.context).asJson)
            )
        )
}
