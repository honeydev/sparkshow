package sparkshow.db.repository

import cats.effect.IO
import doobie.Transactor
import sparkshow.db.model.User
import doobie.implicits._
import cats.effect

class UserRepository(implicit val transactor: Transactor[IO]) {
  // TODO implement
  def getOne(id: Long): IO[Option[User]] = {
    sql"SELECT id, username FROM users WHERE id = ${id}".query[Option[User]].unique.transact(transactor)
  }
  
  def getOne(username: String) = {
    sql"SELECT id, username FROM users WHERE username = ${username}".query[Option[User]].unique.transact(transactor)
  }

  def createOne(user: User) = ???
}
