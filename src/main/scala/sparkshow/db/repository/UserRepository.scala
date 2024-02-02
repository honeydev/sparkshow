package sparkshow.db.repository

import cats.effect.IO
import doobie.Transactor
import sparkshow.db.model.User
import doobie.implicits._
import cats.effect

class UserRepository(implicit val transactor: Transactor[IO]) {

    private val TableName = "users"

    def getOne(id: Long): IO[Option[User]] = {
        sql"select id, username, passwordhash from users where id = ${id}"
            .query[Option[User]]
            .unique
            .transact(transactor)
    }

    def getOne(username: String) = {
        sql"SELECT id, username FROM users WHERE username = ${username}"
            .query[Option[User]]
            .unique
            .transact(transactor)
    }

    def createOne(username: String, email: String, passwordHash: String) = {
        sql"INSERT INTO $TableName (username, email, password_hash) VALUES ($username, $email, $passwordHash)".update
            .withUniqueGeneratedKeys(
              "id",
              "username",
              "email",
              "password_hash"
            )
            .transact(transactor)
    }
}
