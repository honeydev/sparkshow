package sparkshow

import cats.data._
import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import sparkshow.conf.{AppConf, DBConf}
import sparkshow.db.PG
import sparkshow.db.web.data.LoginRequest
import org.flywaydb.core.Flyway
import sparkshow.db.repository.UserRepository
import cats.effect.unsafe.IORuntime
import sparkshow.service.AuthService
import sparkshow.service.UserService

case class User(id: Long, username: String)


trait HttpApp {

  private implicit val loginReqDecoder = LoginRequest.decoder
  private implicit val encodeImportance: Encoder[String] = Encoder.encodeString

  val config = AppConf.load

  val authUser: Kleisli[OptionT[IO, *], Request[IO], User] = {
    // TODO: Implement
    Kleisli(_ => OptionT.liftF(IO(User(id=1, username="test"))))
  }

  val middleware: AuthMiddleware[IO, User] = {
    AuthMiddleware(authUser)
  }

  protected def buildHttpApp(implicit transactor: HikariTransactor[IO]) = {
      val userRepository = new UserRepository
      val authService = new AuthService(userRepository)
      val authRoutes = HttpRoutes.of[IO] {
        case req@POST -> Root / "auth"  => req
          .as[LoginRequest]
          .flatMap(loginRequest => {
            val res = authService.authenticate(loginRequest)
            Ok(res)
          })
      }

      val authenticatedRoutes: AuthedRoutes[User, IO] =
        AuthedRoutes.of {
          case GET -> Root / "auth" as user =>
            val resp = sql"select id, username from users".query[User].stream.transact(transactor)
            Ok(resp)
            // for {
            //   result <- sql"select id from users".query[User].stream.transact(transactor).map(_.toString).intersperse(",")
            //   r <- Ok(result)
            // } yield r
        }

      middleware(authenticatedRoutes) <+> authRoutes
  }
}

object App extends IOApp with HttpApp {


  def run(args: List[String]): IO[ExitCode] = {
    PG.initTransactor(config.db) { implicit transactor: HikariTransactor[IO] =>
      def lift[A,B](f: A => B): Option[A] => Option[B] = a => a map f
      val httpApp = buildHttpApp

      for {
        _ <- transactor.configure { ds => IO { 
          Flyway
            .configure()
            .dataSource(ds)
            .load()
            .migrate()
        }}
        ecode <- EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8081")
          .withHttpApp(httpApp.orNotFound)
          .build
          .use(_ => IO.never)
          .as(ExitCode.Success)
      } yield ecode
    }
  }
}
