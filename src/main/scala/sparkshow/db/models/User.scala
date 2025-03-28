package sparkshow.db.models

case class User(
    id: Long,
    username: String,
    email: Option[String],
    passwordHash: String
) {
    val Table = "users"
}
