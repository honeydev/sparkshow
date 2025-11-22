package sparkshow.web.routes

import cats.effect.IO
import fs2.{Pipe, Stream}
import io.circe.parser._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.typelevel.log4cats._
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.syntax._
import sparkshow.codecs.MetricCodecs._
import sparkshow.db.models.User
import sparkshow.db.repositories.MetricRepository
import sparkshow.services.MetricService
import sparkshow.web.data
import sparkshow.web.data.{GetMetrics, SendMetrics, SendNothing, SendState}

import java.nio.charset.StandardCharsets
import scala.concurrent.duration._

class WSRoutes(
    val metricService: MetricService,
    val metricRepo: MetricRepository
) {

    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    implicit val logger: SelfAwareStructuredLogger[IO] =
        LoggerFactory[IO].getLogger

    def routes(ws: WebSocketBuilder2[IO]): AuthedRoutes[User, IO] = {
        AuthedRoutes
            .of[User, IO] { case authedRequest @ GET -> Root / "ws" as user =>
                var state: SendState = SendNothing()

                val send: Stream[IO, WebSocketFrame] =
                    Stream
                        .awakeEvery[IO](10.second)
                        .evalMap(_ =>
                            state match {
                                case SendMetrics(incomeMsg) => {
                                    metricRepo
                                        .many(incomeMsg.queries)
                                        .map(v =>
                                            WebSocketFrame
                                                .Text(v.asJson.toString)
                                        )
                                }
                                case SendNothing() =>
                                    IO(WebSocketFrame.Text(""))
                            }
                        )

                val receive: Pipe[IO, WebSocketFrame, Unit] =
                    in =>
                        in.evalMap(frameIn => {

                            val parseResult = for {
                                rawData <- frameIn.data.decodeString(
                                  StandardCharsets.UTF_8
                                )
                                parsed <- parse(rawData)
                                requestType <- parsed.hcursor.get[String](
                                  "request"
                                )
                                newState <- {
                                    import sparkshow.codecs.WSMessagesCodecs._

                                    requestType match {
                                        // {"request": "get_metrics", "entity": {"queries": [1, 2, 3]}}
                                        case "get_metrics" =>
                                            parsed.hcursor
                                                .downField("entity")
                                                .as[GetMetrics]
                                                .map(v => data.SendMetrics(v))
                                        case "send_nothing" =>
                                            Right(SendNothing())
                                        case _ =>
                                            Left[String, data.SendState](
                                              s"Unknown message: $requestType"
                                            )
                                    }
                                }
                            } yield (rawData, newState)

                            for {
                                _ <-
                                    info"Fetch message to change state from user ${user}"

                                _ <- parseResult match {
                                    case Right((rawMessage, newState)) => {
                                        for {
                                            _ <-
                                                info"Raw message: $rawMessage, newState: $newState"
                                            _ = state = newState
                                            _ <-
                                                info"Success change state to $newState"
                                        } yield ()
                                    }
                                    case Left(errorMessage: String) => {
                                        logger.error(errorMessage)
                                    }
                                }

                            } yield ()
                        })

                ws.build(send, receive)
            }
    }
}
