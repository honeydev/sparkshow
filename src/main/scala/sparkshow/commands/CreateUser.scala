package sparkshow.commands

import cats.effect._
import distage.{Injector, ModuleDef, Roots}
import doobie.hikari.HikariTransactor
import izumi.reflect.TagK
import org.flywaydb.core.FlywayExecutor.Command
import scopt.OParser
import sparkshow.Bootstrapper
import sparkshow.DiApp.module
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.{RoleRepository, UserRepository}
import sparkshow.service.{AuthService, UserService}
import sparkshow.web.routes.{AuthRoutes, QueryRoutes, RoutesFacade}

case class Args(
    username: String = "",
    password: String = "",
    email: String = "",
    roles: Seq[String] = List()
)

trait Command {

    def run(args: List[String]): IO[ExitCode]
}

// FIXME ошибка izumi при запуске
trait CommandRunner[C <: Command] extends IOApp {

    val module: ModuleDef

    def run(args: List[String]): IO[ExitCode] = {
        val objectGraphResource = Injector[IO]()
            .produce(module, Roots.target[C])

        objectGraphResource
            .use(_.get[C].run(args))
            .as(ExitCode.Success)
    }
}

object CreateUserRunner extends CommandRunner[CreateUser] {

    val module: ModuleDef = new ModuleDef {
        make[AppConf].from(AppConf.load)
        make[HikariTransactor[IO]].fromResource[PGTransactorResource]
        make[UserRepository]
        make[AuthService]
        make[CreateUser]
    }
}

class CreateUser(transactor: HikariTransactor[IO]) extends Command {

    override def run(args: List[String]): IO[ExitCode] = {
        val builder = OParser.builder[Args]
        val parser = {
            import builder._
            OParser.sequence(
              programName("Sparkshow"),
              head("CLI tool for manage app"),
              opt[String]('u', "username").required
                     .action((a, c) => c.copy(username = a))
                  .text("user username"),
                opt[String]('p', "password")
                  .required
                  .action((a, c) => c.copy(password = a))
                  .text("user password"),
              opt[String]('e', "email")
                  .required
                .action((a, c) => c.copy(email = a))
                .text("user email"),
                opt[Seq[String]]('r', "roles")
                  .required
                  .action((a, c) => c.copy(roles = a))
                  .text("user roles")
            )
        }

            OParser.parse(parser, args, Args()) match {
                case Some(parsed) => {
                    IO.print("X")
                }.as(ExitCode.Success)

                    // PG.initTransactor(config.db) {
                    //     implicit transactor: HikariTransactor[IO] =>
                    //         implicit val roleRepo = new RoleRepository
                    //         implicit val userRepo = new UserRepository
                    //         val userService = new UserService
                    //         (for {
                    //             user <- userService
                    //             .createUser(username = parsed.username, password = parsed.password, parsed.email)
                    //             roles <- roleRepo.getMany(user.id)
                    //             _ <- IO.println(s"Success create user: $user with roles: $roles")
                    //         } yield ()).as(ExitCode.Success)
                    // }
            }
    }
}
