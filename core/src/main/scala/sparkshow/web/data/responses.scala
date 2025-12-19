package sparkshow.web.data

import io.circe._
import io.circe.generic.semiauto._
import java.time.Instant
import sparkshow.data.Aggregate
import sparkshow.db.models.Query
import sparkshow.db.models.Source
import sparkshow.db.models.Source.Schema
import sparkshow.db.models.User

case class InvalidResponse(status: String = "error", message: String)

object InvalidResponse:
    given encoder: Encoder[InvalidResponse] =
        deriveEncoder[InvalidResponse]

sealed class SuccessResponse(status: String = "ok")
case class LoginResponse(status: String = "ok", user: User, token: String)
    extends SuccessResponse(status)

object LoginResponse:
    given userEncoder: Encoder[User] = deriveEncoder[User]
        .mapJsonObject(_.remove("token"))
    given jsonEncoder: Encoder[LoginResponse] = deriveEncoder

case class CreateQueryResponse(
    id: Long,
    userId: Long,
    sourceId: Long,
    createdAt: Instant,
    updatedAt: Instant,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0
)

object CreateQueryResponse:
    import sparkshow.db.models.Aggregate.encoder
    given jsonEncoder: Encoder[CreateQueryResponse] = deriveEncoder

    def fromQuery(query: Query): CreateQueryResponse =
        CreateQueryResponse(
          query.id,
          query.userId,
          query.sourceId,
          query.createdAt,
          query.updatedAt,
          query.columns,
          query.grouped,
          query.aggregate,
          query.state,
          query.retries
        )

case class CreateSourceResponse(
    id: Long,
    name: String,
    path: String,
    header: Boolean,
    delimiter: Option[String],
    schema: Schema
)

object CreateSourceResponse:
    given jsonEncoder: Encoder[CreateSourceResponse] = deriveEncoder

    def fromSource(source: Source): CreateSourceResponse =
        CreateSourceResponse(
          source.id,
          source.name,
          source.path,
          source.header,
          source.delimiter,
          source.schema
        )
