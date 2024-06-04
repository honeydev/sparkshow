package sparkshow.commands

import cats.effect.IO
import doobie.hikari.HikariTransactor
import izumi.distage.roles.model.{RoleDescriptor, RoleTask}
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import org.flywaydb.core.Flyway
import sparkshow.conf.AppConf
import sparkshow.db.repository.RoleRepository
import sparkshow.service.UserService

class MigrateTask(
    transactor: HikariTransactor[IO]
) extends RoleTask[IO] {

    override def start(
        roleParameters: RawEntrypointParams,
        freeArgs: Vector[String]
    ): IO[Unit] = {
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
    }
}

object MigrateTask extends RoleDescriptor {
    override def id = "migrate"
}
