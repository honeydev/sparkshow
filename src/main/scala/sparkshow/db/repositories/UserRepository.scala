package sparkshow.db.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import sparkshow.db.models.{Role, User}

class UserRepository(implicit val transactor: Transactor[IO]) {

    def one(id: Long): IO[Option[User]] = {
        sql"SELECT id, username, email, password_hash from users where id = ${id}"
            .query[User]
            .option
            .transact(transactor)
    }

    def one(username: String): IO[Option[User]] = {
        sql"""
             SELECT
               id,
               username,
               email,
               password_hash
             FROM users
              WHERE username = $username
              """
            .query[User]
            .option
            .transact(transactor)
    }

    def createOne(
        username: String,
        email: String,
        passwordHash: String,
        roles: List[Role]
    ): IO[User] = {
        val createUser =
            sql"""
                 INSERT INTO users (username, email, password_hash)
                 VALUES ($username, $email, $passwordHash)""".update
                .withUniqueGeneratedKeys[User](
                  "id",
                  "username",
                  "email",
                  "password_hash"
                )
        val rolesNames = roles.map(_.name).mkString(", ")

        (for {
            user <- createUser
            rolesId <- sql"SELECT id FROM roles WHERE name IN ($rolesNames)"
                .query[Long]
                .to[List]
            _ <- {
                val rolesUsersIds = rolesId.map(roleId => (roleId, user.id))
                val q =
                    "INSERT INTO users_roles (role_id, user_id) VALUES (?, ?)"
                Update[(Long, Long)](q).updateMany(rolesUsersIds)
            }
        } yield user).transact(transactor)
    }
}
