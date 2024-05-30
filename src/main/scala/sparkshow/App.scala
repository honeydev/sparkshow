package sparkshow

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import distage.{Injector, ModuleDef, Roots}
import doobie.hikari.HikariTransactor
import izumi.distage.model.definition.Lifecycle
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import org.flywaydb.core.Flyway
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Server
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import sparkshow.web.routes.{AuthRoutes, QueryRoutes, RoutesFacade}

import scala.annotation.unused


final case class HttpServer(
                               server: Server
                           )

object HttpServer {

    final case class Impl(transactor: HikariTransactor[IO], routesFacade: RoutesFacade) extends Lifecycle.Of[IO, HttpServer](
        Lifecycle.fromCats {
// FIXME not worked now
            transactor
                .configure { ds =>
                    IO {
                        Flyway
                            .configure()
                            .dataSource(ds)
                            .load()
                            .migrate()
                    }
                }
            EmberServerBuilder
                .default[IO]
                .withHost(
                    ipv4"0.0.0.0"
                )
                .withPort(
                    port"8081"
                )
                .withHttpApp(routesFacade.routes.orNotFound)
                .build
                .map(HttpServer(_))
        }
    )
}

class AppServiceRole(@unused httpServer: HttpServer) extends RoleService[IO] {


    override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): Lifecycle[IO, Unit] = {
        Lifecycle.liftF(IO.println("Start server"))
        // val res = for {
        //   _ <- transactor
        //     .configure { ds =>
        //       IO {
        //         Flyway
        //           .configure()
        //           .dataSource(ds)
        //           .load()
        //           .migrate()
        //       }
        //     }
        //   ecode <- EmberServerBuilder
        //     .default[IO]
        //     .withHost(
        //       ipv4"0.0.0.0"
        //     )
        //     .withPort(
        //       port"8081"
        //     )
        //     .withHttpApp(routesFacade.routes.orNotFound)
        //     .build
        //      .use(_ => IO.never)
        //      .as(
        //        ExitCode.Success
        //      )
        // } yield ()

    }


    //    Lifecycle.liftF {
    //      EmberServerBuilder
    //        .default[IO]
    //        .withHost(
    //          ipv4"0.0.0.0"
    //        )
    //        .withPort(
    //          port"8081"
    //        )
    //        .withHttpApp(routesFacade.routes.orNotFound)
    //        .build
    ////        .use(_ => IO.never)
    ////        .as(
    ////          ExitCode.Success
    ////        )
    //    }


}

object AppServiceRole extends RoleDescriptor {
    override def id = "web"
}

class Bootstrapper(transactor: HikariTransactor[IO], routesFacade: RoutesFacade) {
    val run: IO[ExitCode] = for {
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
