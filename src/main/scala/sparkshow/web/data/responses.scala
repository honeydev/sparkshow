package sparkshow.web.data

import io.circe._
import io.circe.generic.semiauto._
import sparkshow.db.models.{Aggregate, Query, User}

case class InvalidResponse(status: String = "error", message: String)

object InvalidResponse {
    implicit val encoder: Encoder[InvalidResponse] =
        deriveEncoder[InvalidResponse]
}

sealed class SuccessResponse(status: String = "ok")
case class LoginResponse(status: String = "ok", user: User, token: String)
    extends SuccessResponse(status)

object LoginResponse {
    implicit val userEncoder: Encoder[User] = deriveEncoder[User]
        .mapJsonObject(_.remove("passwordHash"))
    implicit val jsonEncoder: Encoder[LoginResponse] = deriveEncoder
}

case class CreateQueryResponse(query: Query)

object CreateQueryResponse {

    import sparkshow.db.models.Aggregate.encoder

    implicit val queryEncoder: Encoder[Query] = deriveEncoder[Query]
    implicit val jsonEncoder: Encoder[CreateQueryResponse] = deriveEncoder
}
