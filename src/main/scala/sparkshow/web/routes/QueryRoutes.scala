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
import sparkshow.db.web.data.QueryRequest

class QueryRoutes(
    val userService: UserService,
    val conf: AppConf
) extends CommonRoutesUtils {

    val routes: Kleisli[OptionT[IO, *], Request[IO], Response[IO]] = mw(
      AuthedRoutes
        .of { case POST -> Root / "query" as _ =>
         Ok("stub")
      }
    )
}

