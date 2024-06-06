package sparkshow

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.HC
import doobie.util.transactor.Transactor
import izumi.distage.model.definition.ModuleDef
import izumi.distage.testkit.scalatest.AssertCIO
import izumi.distage.testkit.scalatest.Spec1
import org.flywaydb.core.Flyway
import sparkshow.conf.AppConf
import izumi.distage.testkit.model.TestConfig

trait BaseIntegrationSpec extends Spec1[IO] with AssertCIO {
    override def config: TestConfig = super.config.copy(
      moduleOverrides = new ModuleDef {
          make[Transactor[IO]].from { appConfig: AppConf =>
              val ta = Transactor.fromDriverManager[IO](
                "org.postgresql.Driver", // JDBC driver classname
                url = appConfig.db.url, // Connect URL
                user = appConfig.db.username, // Database user name
                pass = appConfig.db.password // Database password
                // Don't setup logging for now. See Logging page for how to log events in detail
              )
              ta.configure { _ =>
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
              }.unsafeRunSync()
              Transactor.after.set(ta, HC.rollback)
          }
      },
      debugOutput = true
    )
}
