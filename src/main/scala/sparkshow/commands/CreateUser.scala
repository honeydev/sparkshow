package sparkshow.commands

import cats.effect._
import doobie.hikari.HikariTransactor
import scopt.OParser
import sparkshow.conf.AppConf
import sparkshow.db.PG
import sparkshow.db.repository.{RoleRepository, UserRepository}
import sparkshow.service.UserService

case class Args(
    username: String = "",
    password: String = "",
    email: String = "",
    roles: Seq[String] = List()
)

object CreateUser extends IOApp {

    val config = AppConf.load

    def run(args: List[String]) = {
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
                case Some(parsed) =>
                    PG.initTransactor(config.db) {
                        implicit transactor: HikariTransactor[IO] =>
                            implicit val roleRepo = new RoleRepository
                            implicit val userRepo = new UserRepository
                            val userService = new UserService
                            (for {
                                user <- userService
                                .createUser(username = parsed.username, password = parsed.password, parsed.email)
                                roles <- roleRepo.getMany(user.id)
                                _ <- IO.println(s"Success create user: $user with roles: $roles")
                            } yield ()).as(ExitCode.Success)
                    }
            }
    }
}
