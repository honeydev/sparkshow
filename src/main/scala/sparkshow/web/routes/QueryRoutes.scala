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

class QueryRoutes(val userService: UserService) {

    val user: User = User(1, "t", Some("t"), "t")
    val authUser: Kleisli[OptionT[IO, *], Request[IO], User] =
        Kleisli(_ =>
            OptionT.liftF({

                IO(user)
            })
        )

    // Kleisli[OptionT[F, *], Request[F], T]
    val mw: AuthMiddleware[IO, User] = AuthMiddleware(authUser)

    val routes: Kleisli[OptionT[IO, *], Request[IO], Response[IO]] = mw(
      AuthedRoutes
          .of { case POST -> Root / "query" as _ =>
              Ok("Stub query")
          }
    )
}

