package sparkshow

import org.scalatest.flatspec.AnyFlatSpec
import sparkshow.conf.AppConf
import cats.effect.IOApp
import sparkshow.db.PG
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import org.flywaydb.core.Flyway
import doobie.hikari.HikariTransactor
import doobie._
import doobie.implicits._
import cats.effect.unsafe.implicits.global
import cats.effect.unsafe._
import org.http4s.Request
import org.http4s.Method
import io.circe.syntax._
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.scalatest.BeforeAndAfter
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import sparkshow.conf.DBConf
import sparkshow.service.UserService

import org.scalamock.scalatest.MockFactory

class ExampleTestSuite extends AnyFlatSpec with HttpApp with BeforeAndAfter {

    def testTransactor(
        config: DBConf
    ) = {
        for {
            ec <- ExecutionContexts
                .fixedThreadPool[
                  IO
                ](1)
            t <- HikariTransactor
                .newHikariTransactor[
                  IO
                ](
                  driverClassName =
                      "org.postgresql.Driver", // JDBC driver classname
                  url = config.url, // Connect URL
                  user = config.username, // Database user name
                  pass =
                      config.password, // Database password     // Don't setup logging for now. See Logging page for how to log events in detail
                  connectEC = ec
                )
        } yield (t)
    }

    before {
        testTransactor(
          config.db
        ).use(
          (transactor: HikariTransactor[
            IO
          ]) => {
              transactor
                  .configure { ds =>
                      IO {
                          Flyway
                              .configure()
                              .dataSource(
                                ds
                              )
                              .load()
                              .migrate()
                      }
                  }
                  .unsafeRunSync()
              IO(
                ExitCode.Success
              )
          }
        ).unsafeRunSync
    }

    after {
        testTransactor(
          config.db
        ).use(
          (transactor: HikariTransactor[
            IO
          ]) => {
              transactor
                  .configure { ds =>
                      IO {
                          Flyway
                              .configure()
                              .cleanDisabled(
                                false
                              )
                              .dataSource(ds)
                              .load()
                              .clean()
                      }
                  }
                  .unsafeRunSync()
              IO(
                ExitCode.Success
              )
          }
        ).unsafeRunSync
    }

    // test to
    "test" should "sucdess" in {
        val conf =
            AppConf.load
        testTransactor(
          config.db
        ).use((transactor: HikariTransactor[IO]) => {
            val httpApp =
                buildHttpApp(
                  transactor
                )
            val res = httpApp.orNotFound
                .run(
                  Request(
                    method = Method.GET,
                    uri = uri"/auth"
                  )
                )
                .unsafeRunSync

            val response = res
                .as[
                  String
                ]
                .unsafeRunSync
            IO(
              ExitCode.Success
            )
        }).unsafeRunSync()
        assert(true)
    }
}
