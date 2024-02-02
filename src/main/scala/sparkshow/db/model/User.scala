package sparkshow.db.model

case class User(
    username: String,
    email: Option[
      String
    ],
    passwordHash: String
)
