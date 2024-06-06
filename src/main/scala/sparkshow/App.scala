package sparkshow

import scala.annotation.unused

import cats.effect.IO
import com.comcast.ip4s.IpLiteralSyntax
import doobie.util.transactor.Transactor
import izumi.distage.model.definition.Lifecycle
import izumi.distage.roles.model.RoleDescriptor
import izumi.distage.roles.model.RoleService
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Server
import sparkshow.web.routes.RoutesFacade

final case class HttpServer(
    server: Server
)

object HttpServer {

    final case class Impl(
        transactor: Transactor[IO],
        routesFacade: RoutesFacade
    ) extends Lifecycle.Of[IO, HttpServer](
          Lifecycle.fromCats {
              EmberServerBuilder
                  .default[IO]
                  .withHost(
                    ipv4"0.0.0.0"
                  )
                  .withPort(
                    port"8081"
                  )
                  .withHttpApp(routesFacade.build.orNotFound)
                  .build
                  .map(HttpServer(_))
          }
        )
}

class AppServiceRole(@unused httpServer: HttpServer) extends RoleService[IO] {

    override def start(
        roleParameters: RawEntrypointParams,
        freeArgs: Vector[String]
    ): Lifecycle[IO, Unit] = {
        Lifecycle.liftF(IO.println("Start server"))
    }
}

object AppServiceRole extends RoleDescriptor {
    override def id = "web"
}
