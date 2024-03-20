package sparkshow.db.model

import sparkshow.db.model.RoleName.RoleName

object RoleName extends Enumeration {
  type RoleName = Value
  val USER, MANAGER, ADMIN = Value
}

case class Role(
    id: Option[Long] = None,
    name: String
) {
    val Table = "roles"
}
