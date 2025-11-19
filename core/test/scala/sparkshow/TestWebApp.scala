package sparkshow

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import sparkshow.web.routes.{AuthRoutes, JWTMiddleware, QueryRoutes, RoutesFacade, SourceRoutes}
import cats.implicits._
import org.http4s.{Request, Response}

class TestWebApp(
                    routes: RoutesFacade,
                    authRoutes: AuthRoutes,
                    queryRoutes: QueryRoutes,
                    sourceRoutes: SourceRoutes,
                    JWTMiddleware: JWTMiddleware
                ) {


    def routes: Kleisli[IO, Request[IO], Response[IO]] = {
        val testRoutes = authRoutes.routes <+> JWTMiddleware.mw(
            queryRoutes.routes <+> sourceRoutes.routes
        )
        ErrorHandling.Recover.total(
            ErrorAction.log(
                testRoutes,
                messageFailureLogAction = errorHandler,
                serviceErrorLogAction   = errorHandler
            )
        ).orNotFound
    }
    private def errorHandler(t: Throwable, msg: => String): OptionT[IO, Unit] =
        OptionT.liftF(
            IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace())
        )
}
