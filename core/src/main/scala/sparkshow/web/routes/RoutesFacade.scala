package sparkshow.web.routes

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import org.http4s.Request
import org.http4s.Response
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.http4s.server.websocket.WebSocketBuilder2
import sparkshow.conf.AppConf
import sparkshow.services.UserService

class RoutesFacade(
    val authRoutes: AuthRoutes,
    val queryRoutes: QueryRoutes,
    val sourceRoutes: SourceRoutes,
    val userService: UserService,
    val JWTMiddleware: JWTMiddleware,
    val wsRoutes: WSRoutes,
    val conf: AppConf
) {

    def build(
        ws: WebSocketBuilder2[IO]
    ): Kleisli[OptionT[IO, *], Request[IO], Response[IO]] = {
        val appRoutes: Kleisli[OptionT[IO, *], Request[IO], Response[IO]] =
            authRoutes.routes <+> JWTMiddleware.mw(
              queryRoutes.routes <+> sourceRoutes.routes <+> wsRoutes.routes(ws)
            )

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
