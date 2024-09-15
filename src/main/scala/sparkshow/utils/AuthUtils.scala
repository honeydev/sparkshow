package sparkshow.utils

import io.circe._
import io.circe.generic.auto._
import io.circe.jawn.{parse => jawnParse}
import io.circe.syntax.EncoderOps
import java.time.Instant
import pdi.jwt.JwtCirce
import sparkshow.db.model.User

sealed case class JwtPayload(
    expires: Long,
    username: String,
    email: Option[String],
    id: Long
)

object AuthUtils {

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
}
