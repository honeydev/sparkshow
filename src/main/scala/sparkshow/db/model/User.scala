package sparkshow.db.model

case class User(
    id: Long,
    username: String,
    email: Option[String],
    passwordHash: String
) {
    val Table = "users"
}
