package sparkshow.web.routes

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import sparkshow.db.model.User
import sparkshow.service.UserService
import org.http4s.headers.Authorization
import org.http4s.Credentials.Token
import sparkshow.utils.AuthUtils
import sparkshow.conf.AppConf
import cats.data.EitherT
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import sparkshow.db.web.data.InvalidResponse

trait CommonRoutesUtils {

    val userService: UserService
    val conf: AppConf

    protected val mw: AuthMiddleware[IO, User] =
        AuthMiddleware.apply[IO, String, User](authUser, onFailure)

    private val authUser: Kleisli[IO, Request[IO], Either[String, User]] =
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

    private val onFailure: AuthedRoutes[String, IO] =
        Kleisli(req =>
            OptionT.liftF(
              Forbidden(InvalidResponse(message = req.context).asJson)
            )
        )

}
