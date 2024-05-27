package sparkshow.commands

import cats.effect._
import distage.plugins.PluginDef
import doobie.hikari.HikariTransactor
import izumi.distage.roles.model.{RoleDescriptor, RoleTask}
import izumi.distage.roles.model.definition.RoleModuleDef
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import scopt.OParser

case class Args(
                 username: String = "",
                 password: String = "",
                 email: String = "",
                 roles: Seq[String] = List()
               )


class CreateUserTask(appConf: AppConf) extends RoleTask[IO] {
  def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): IO[Unit] = {
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

            OParser.parse(parser, freeArgs, Args()) match {
                case Some(parsed) => {
                    IO.print("X")
                }.as(ExitCode.Success)
                

         

  }
}}

object CreateUserTask extends RoleDescriptor {
  override def id = "create-user"
}


// class CreateUser(transactor: HikariTransactor[IO]) {
// 
//     override def run(args: Vector[String]): IO[ExitCode] = {
//         val builder = OParser.builder[Args]
//         val parser = {
//             import builder._
//             OParser.sequence(
//               programName("Sparkshow"),
//               head("CLI tool for manage app"),
//               opt[String]('u', "username").required
//                      .action((a, c) => c.copy(username = a))
//                   .text("user username"),
//                 opt[String]('p', "password")
//                   .required
//                   .action((a, c) => c.copy(password = a))
//                   .text("user password"),
//               opt[String]('e', "email")
//                   .required
//                 .action((a, c) => c.copy(email = a))
//                 .text("user email"),
//                 opt[Seq[String]]('r', "roles")
//                   .required
//                   .action((a, c) => c.copy(roles = a))
//                   .text("user roles")
//             )
//         }
// 
//             OParser.parse(parser, args, Args()) match {
//                 case Some(parsed) => {
//                     IO.print("X")
//                 }.as(ExitCode.Success)
// 
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
//             }
//     }
// }
