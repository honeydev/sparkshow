package sparkshow.db.model

case class User(username: String, passwordHash: String, id: Option[Long] = None)
