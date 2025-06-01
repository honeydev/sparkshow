package sparkshow.web.data

import io.circe._
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.generic.semiauto._
import sparkshow.db.models.Source.Schema
import sparkshow.db.models.{Aggregate, Query, Source, User}

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

@ConfiguredJsonCodec
case class CreateQueryResponse(
    id: Long,
    userId: Long,
    sourceId: Long,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0
)

object CreateQueryResponse {
    implicit val jsonEncoder: Encoder[CreateQueryResponse] = deriveEncoder
    implicit val customConfig: Configuration =
        Configuration.default.withSnakeCaseMemberNames

    def fromQuery(query: Query): CreateQueryResponse =
        CreateQueryResponse(
          query.id,
          query.userId,
          query.sourceId,
          query.columns,
          query.grouped,
          query.aggregate,
          query.state,
          query.retries
        )
}

@ConfiguredJsonCodec
case class CreateSourceResponse(
    id: Long,
    name: String,
    path: String,
    header: Boolean,
    delimiter: Option[String],
    schema: Schema
)

object CreateSourceResponse {
    implicit val jsonEncoder: Encoder[CreateQueryResponse] = deriveEncoder
    implicit val customConfig: Configuration =
        Configuration.default.withSnakeCaseMemberNames

    def fromSource(source: Source): CreateSourceResponse =
        CreateSourceResponse(
          source.id,
          source.name,
          source.path,
          source.header,
          source.delimiter,
          source.schema
        )
}
