package sparkshow

import cats.data._
import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.circe.Encoder
import io.circe.generic.auto._
import org.flywaydb.core.Flyway
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import sparkshow.conf.AppConf
import sparkshow.db.PG
import sparkshow.db.model.User
import sparkshow.db.repository.UserRepository
import sparkshow.db.web.data.LoginRequest
import sparkshow.service.AuthService

case class User(
    id: Long,
    username: String,
    passwordHash: String
)

trait HttpApp {

    private implicit val loginReqDecoder =
        LoginRequest.decoder
    private implicit val encodeImportance: Encoder[
      String
    ] =
        Encoder.encodeString

    val config =
        AppConf.load

    val authUser: Kleisli[
      OptionT[
        IO,
        *
      ],
      Request[
        IO
      ],
      User
    ] = {
        // TODO: Implement
        Kleisli(_ =>
            OptionT.liftF(
              IO(
                User(
                  id = 1,
                  username = "test",
                  passwordHash = "qaz"
                )
              )
            )
        )
    }

    val middleware: AuthMiddleware[
      IO,
      User
    ] = {
        AuthMiddleware(
          authUser
        )
    }

    protected def buildHttpApp(implicit
        transactor: HikariTransactor[
          IO
        ]
    ) = {
        val userRepository =
            new UserRepository
        val authService =
            new AuthService(
              userRepository
            )
        val authRoutes = HttpRoutes
            .of[
              IO
            ] { case req @ POST -> Root / "auth" =>
                req
                    .as[LoginRequest]
                    .flatMap(loginRequest => {
                        val res = authService
                            .authenticate(
                              loginRequest
                            )
                        Ok(res)
                    })
            }

        val authenticatedRoutes: AuthedRoutes[
          User,
          IO
        ] =
            AuthedRoutes
                .of { case GET -> Root / "auth" as user =>
                    val resp = sql"select id, username from users"
                        .query[User]
                        .stream
                        .transact(
                          transactor
                        )
                    Ok(
                      resp
                    )
                }

        val routes =
            authRoutes <+> middleware(
              authenticatedRoutes
            )

        routes
    }
}

object App extends IOApp with HttpApp {

    def run(
        args: List[
          String
        ]
    ): IO[
      ExitCode
    ] = {
        PG.initTransactor(
          config.db
        ) {
            implicit transactor: HikariTransactor[
              IO
            ] =>
                val httpApp =
                    buildHttpApp

                val errorRoute = HttpRoutes.of[
                  IO
                ] { case req @ GET -> Root / "err" =>
                    throw new Exception(
                      "Hex don't swallow me"
                    )
                }

                def errorHandler(
                    t: Throwable,
                    msg: => String
                ): OptionT[
                  IO,
                  Unit
                ] =
                    OptionT.liftF(
                      IO.println(
                        msg
                      ) >>
                          IO.println(
                            t
                          ) >>
                          IO(
                            t.printStackTrace()
                          )
                    )

                val withErrorLogging = ErrorHandling.Recover.total(
                  ErrorAction.log(
                    errorRoute,
                    messageFailureLogAction = errorHandler,
                    serviceErrorLogAction = errorHandler
                  )
                )

                for {
                    _ <- transactor
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
                    ecode <- EmberServerBuilder
                        .default[IO]
                        .withHost(
                          ipv4"0.0.0.0"
                        )
                        .withPort(
                          port"8081"
                        )
                        .withHttpApp(
                          withErrorLogging.orNotFound
                        )
                        .build
                        .use(_ => IO.never)
                        .as(
                          ExitCode.Success
                        )
                } yield ecode
        }
    }
}
