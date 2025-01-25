package sparkshow

import cats.effect.IO
import de.lhns.doobie.flyway.BaselineMigrations.BaselineMigrationOps
import de.lhns.doobie.flyway.Flyway
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import izumi.distage.docker.bundled.PostgresFlyWayDocker
import izumi.distage.docker.modules.DockerSupportModule
import izumi.distage.model.definition.ModuleDef
import izumi.distage.testkit.model.TestConfig
import izumi.distage.testkit.scalatest.{AssertCIO, Spec1}
import izumi.reflect.TagK

class PostgresDockerModule[IO[_]: TagK] extends ModuleDef {
    make[PostgresFlyWayDocker.Container].fromResource {
        PostgresFlyWayDocker.make[IO]
    }
}

object PostgresDockerModule {
    def apply[F[_]: TagK] = new PostgresDockerModule[F]
}

final case class PostgresServerConfig(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String
)

trait BaseIntegrationSpec extends Spec1[IO] with AssertCIO {
    override def config: TestConfig = super.config.copy(
      moduleOverrides = new ModuleDef {
          include(DockerSupportModule[IO])
          include(PostgresDockerModule[IO])

          make[PostgresServerConfig].from {
              container: PostgresFlyWayDocker.Container =>
                  {
                      val knownAddress = container.availablePorts.first(
                        PostgresFlyWayDocker.primaryPort
                      )
                      PostgresServerConfig(
                        host     = knownAddress.hostString,
                        port     = knownAddress.port,
                        database = "postgres",
                        username = "postgres",
                        password = "postgres"
                      )
                  }
          }

          make[Transactor[IO]].fromResource {
              (
                config: PostgresServerConfig
              ) =>
                  {
                      for {
                          ec <- ExecutionContexts.fixedThreadPool[IO](8)
                          xa <- HikariTransactor.newHikariTransactor[IO](
                            "org.postgresql.Driver",
                            s"jdbc:postgresql://${config.host}:${config.port}/${config.database}",
                            config.username,
                            config.password,
                            ec
                          )
                          // TODO migrate with PostgresFlyWayDocker
                          _ <- Flyway(xa) { flyway =>
                              for {
                                  info <- flyway.info()
                                  _ <- flyway
                                      .configure(
                                        _.withBaselineMigrate(info)
                                            .validateMigrationNaming(true)
                                      )
                                      .migrate()
                              } yield ()
                          }

                      } yield xa
                  }
          }
      },
      debugOutput = true
    )
}
