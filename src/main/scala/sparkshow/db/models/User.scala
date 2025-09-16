package sparkshow.db.models

import java.time.Instant

case class User(
    id: Long,
    createdAt: Instant,
    updatedAt: Instant,
    username: String,
    email: Option[String],
    passwordHash: String
)