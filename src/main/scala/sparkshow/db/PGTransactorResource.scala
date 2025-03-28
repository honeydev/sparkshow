package sparkshow.db

import cats.effect._
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import izumi.functional.lifecycle.Lifecycle.OfCats
import sparkshow.conf.AppConf

class PGTransactorResource(
    appConfig: AppConf
) extends OfCats(
      for {
          ec <- ExecutionContexts.fixedThreadPool[IO](8)
          ta <- HikariTransactor.newHikariTransactor[IO](
            driverClassName = "org.postgresql.Driver", // JDBC driver classname
            url             = appConfig.db.url, // Connect URL
            user            = appConfig.db.username, // Database user name
            pass =
                appConfig.db.password, // Database password
            // Don't setup logging for now. See Logging page for how to log events in detail
            connectEC = ec
          )
      } yield ta
    )
