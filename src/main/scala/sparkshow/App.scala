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

case class User(id: Long)

case class Resources(transactor: HikariTransactor[IO])

object App extends IOApp {

  private implicit val loginReqDecoder = LoginRequest.decoder
  private implicit val encodeImportance: Encoder[String] = Encoder.encodeString

  val config = AppConf.load

  val authUser: Kleisli[OptionT[IO, *], Request[IO], User] =
    Kleisli(_ => OptionT.liftF(IO(User(id=1))))

  val middleware: AuthMiddleware[IO, User] = {
    AuthMiddleware(authUser)
  }

  def run(args: List[String]): IO[ExitCode] = {
    PG.initTransactor(config.db) { transactor =>
      val authRoutes = HttpRoutes.of[IO] {
        case req@POST -> Root / "auth" / "login" => req
          .as[LoginRequest]
          .flatMap(v => {
            Ok(v.username)
          })
      }

      val authenticatedRoutes: AuthedRoutes[User, IO] =
        AuthedRoutes.of {
          case GET -> Root / "auth" as user =>
            for {
              result <- sql"select 42".query[Int].unique.transact(transactor)
              r <- Ok(result)
            } yield r
        }

      val httpApp = middleware(authenticatedRoutes) <+> authRoutes

      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpApp(httpApp.orNotFound)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }
  }
}
