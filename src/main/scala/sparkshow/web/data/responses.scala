package sparkshow.db.web.data

import io.circe._
import io.circe.generic.semiauto._
import sparkshow.db.model.User

case class InvalidResponse(message: String, status: String = "error")
sealed class SuccessResponse(status: String = "ok")
case class LoginResponse(user: User, token: String) extends SuccessResponse

object LoginResponse {
    implicit val userEncoder: Encoder[User] = deriveEncoder[User]
        .mapJsonObject(_.remove("passwordHash"))
    implicit val jsonEncoder: Encoder[LoginResponse] = deriveEncoder
}
