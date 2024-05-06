package sparkshow.web.routes

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}

class RoutesFacade(val authRoutes: AuthRoutes, val queryRoutes: QueryRoutes) {
    val withErrorLogging = ErrorHandling.Recover.total(
      ErrorAction.log(
        appRoutes,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler
      )
    )
    val routes = withErrorLogging
    private val appRoutes = authRoutes.routes <+> queryRoutes.routes

    def errorHandler(t: Throwable, msg: => String): OptionT[IO, Unit] =
        OptionT.liftF(
          IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace())
        )
}
