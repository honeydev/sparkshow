package sparkshow.db

import cats.effect.{ExitCode, IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import sparkshow.App.config
import sparkshow.conf.DBConf

case class PG(transactor: Resource[IO, HikariTransactor[IO]]) {

}


object PG {
  val  ThreadPoolSize = 4

  def initTransactor(config: DBConf)(f: HikariTransactor[IO] => IO[ExitCode]): IO[ExitCode] = {
    val transcatorResoruce: Resource[IO, HikariTransactor[IO]] = for {
      ec <- ExecutionContexts.fixedThreadPool[IO](ThreadPoolSize)
      t <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver", // JDBC driver classname
        url = config.url, // Connect URL
        user = config.username, // Database user name
        pass = config.password, // Database password     // Don't setup logging for now. See Logging page for how to log events in detail
        connectEC = ec
      )
    } yield (t)
    transcatorResoruce.use(f)
  }
}
