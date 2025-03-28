package sparkshow.commands

import cats.effect._
import izumi.distage.roles.model.{RoleDescriptor, RoleTask}
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import scopt.OParser
import sparkshow.conf.AppConf
import sparkshow.db.repositories.RoleRepository
import sparkshow.services.UserService

case class Args(
    username: String   = "",
    password: String   = "",
    email: String      = "",
    roles: Seq[String] = List()
)

class CreateUserTask(
    appConf: AppConf,
    userService: UserService,
    roleRepo: RoleRepository
) extends RoleTask[IO] {
    def start(
        roleParameters: RawEntrypointParams,
        freeArgs: Vector[String]
    ): IO[Unit] = {
        IO.println(s"Running ${CreateUserTask.id}! with $appConf")
        val builder = OParser.builder[Args]
        val parser = {
            import builder._
            OParser.sequence(
              programName("Sparkshow"),
              head("CLI tool for manage app"),
              opt[String]('u', "username").required
                  .action((a, c) => c.copy(username = a))
                  .text("user username"),
              opt[String]('p', "password").required
                  .action((a, c) => c.copy(password = a))
                  .text("user password"),
              opt[String]('e', "email").required
                  .action((a, c) => c.copy(email = a))
                  .text("user email"),
              opt[Seq[String]]('r', "roles").required
                  .action((a, c) => c.copy(roles = a))
                  .text("user roles")
            )
        }

        OParser.parse(
          parser,
          freeArgs.slice(1, freeArgs.length),
          Args()
        ) match {
            case Some(parsed) =>
                (for {
                    user <- userService.createUser(
                      username = parsed.username,
                      password = parsed.password,
                      parsed.email
                    )
                    roles <- roleRepo.many(user.id)
                    _ <- IO.println(
                      s"Success create user: $user with roles: $roles"
                    )
                } yield ()).as(ExitCode.Success)
        }
    }
}

object CreateUserTask extends RoleDescriptor {
    override def id = "create-user"
}
