package sparkshow.db.models

import java.time.Instant

object RoleName extends Enumeration {
    type RoleName = Value
    val USER, MANAGER, ADMIN = Value
}

case class Role(
    id: Option[Long] = None,
    createdAt: Instant,
    updatedAt: Instant,
    name: String
)
