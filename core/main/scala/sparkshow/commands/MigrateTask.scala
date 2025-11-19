package sparkshow.commands

import cats.effect.IO
import doobie.util.transactor.Transactor
import izumi.distage.roles.model.{RoleDescriptor, RoleTask}
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import org.flywaydb.core.Flyway
import sparkshow.conf.AppConf

class MigrateTask(
    transactor: Transactor[IO],
    appConfig: AppConf
) extends RoleTask[IO] {

    override def start(
        roleParameters: RawEntrypointParams,
        freeArgs: Vector[String]
    ): IO[Unit] = {
        transactor
            .configure { _ =>
                IO {
                    Flyway
                        .configure()
                        .dataSource(
                          appConfig.db.url,
                          appConfig.db.username,
                          appConfig.db.password
                        )
                        .load()
                        .migrate()
                }
            }
    }
}

object MigrateTask extends RoleDescriptor {
    override def id = "migrate"
}
