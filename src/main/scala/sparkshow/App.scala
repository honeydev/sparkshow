package sparkshow

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import distage.{Injector, ModuleDef, Roots}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.http4s.ember.server._
import org.http4s.implicits._
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import sparkshow.web.routes.{AuthRoutes, QueryRoutes, RoutesFacade}


class Bootstrapper(transactor: HikariTransactor[IO], routesFacade: RoutesFacade) {
    val run = for {
        _ <- transactor
            .configure { ds =>
                IO {
                    Flyway
                        .configure()
                        .dataSource(ds)
                        .load()
                        .migrate()
                }
            }
        ecode <- EmberServerBuilder
            .default[IO]
            .withHost(
                ipv4"0.0.0.0"
            )
            .withPort(
                port"8081"
            )
            .withHttpApp(routesFacade.routes.orNotFound)
            .build
            .use(_ => IO.never)
            .as(
                ExitCode.Success
            )
    } yield ecode
}

object DiApp extends IOApp {

    private val module = new ModuleDef {
        make[AppConf].from(AppConf.load)
        make[HikariTransactor[IO]].fromResource[PGTransactorResource]
        make[UserRepository]
        make[AuthService]
        make[AuthRoutes]
        make[QueryRoutes]
        make[RoutesFacade]
        make[Bootstrapper]
    }

    def run(args: List[String]): IO[ExitCode] = {
        val objectGraphResource = Injector[IO]()
            .produce(module, Roots.target[Bootstrapper])

        objectGraphResource
            .use(_.get[Bootstrapper].run)
            .as(ExitCode.Success)
    }
}
