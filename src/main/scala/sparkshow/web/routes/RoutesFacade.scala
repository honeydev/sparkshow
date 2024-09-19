package sparkshow.web.routes

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.implicits._
import org.http4s.{Request, Response}
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import sparkshow.conf.AppConf
import sparkshow.service.UserService

class RoutesFacade(
    val authRoutes: AuthRoutes,
    val queryRoutes: QueryRoutes,
    val userService: UserService,
    val JWTMiddleware: JWTMiddleware,
    val conf: AppConf
) {

    def build: Kleisli[OptionT[IO, *], Request[IO], Response[IO]] = {
        val appRoutes: Kleisli[OptionT[IO, *], Request[IO], Response[IO]] =
            authRoutes.routes <+> JWTMiddleware.mw(queryRoutes.routes)

        ErrorHandling.Recover.total(
          ErrorAction.log(
            appRoutes,
            messageFailureLogAction = errorHandler,
            serviceErrorLogAction   = errorHandler
          )
        )
    }

    private def errorHandler(t: Throwable, msg: => String): OptionT[IO, Unit] =
        OptionT.liftF(
          IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace())
        )
}
