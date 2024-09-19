package sparkshow.utils

import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.jawn.{decode, parse => jawnParse}
import io.circe.syntax.EncoderOps
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import sparkshow.db.model.User

import java.time.Instant
import scala.util.{Failure, Success}

sealed case class JwtPayload(
    expires: Long,
    username: String,
    email: Option[String],
    id: Long
)

object AuthUtils {

    implicit val decoder = deriveDecoder[JwtPayload]

    def encodeToken(user: User, secret: String): String = {
        val tokenData = JwtPayload(
          expires  = Instant.now.getEpochSecond,
          username = user.username,
          email    = user.email,
          id       = user.id
        ).asJson
        val Right(header) = jawnParse("""{"typ":"JWT","alg":"HS256"}""")
        JwtCirce.encode(header, tokenData, secret)
    }

    def decodeToken(
        token: String,
        secret: String
    ): Either[String, JwtPayload] = {

        JwtCirce.decode(token, secret, Seq(JwtAlgorithm.HS256)) match {
            case Success(value: JwtClaim) =>
                decode(value.content) match {
                    case Left(error) => {
                        error.printStackTrace()
                        Left(error.toString)
                    }
                    case Right(payload) => Right(payload)
                }
            case Failure(error) => {
                println(error) // TODO replace on logger
                error.printStackTrace()
                Left("Invalid decode JWT token")
            }
        }
    }
}
