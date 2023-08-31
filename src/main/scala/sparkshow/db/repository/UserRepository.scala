package sparkshow.db.repository

import cats.effect.IO
import doobie.Transactor
import sparkshow.db.model.User

class UserRepository(val transactor: Transactor[IO]) {
  // TODO implement
  def getOne(id: Long): User = {
    ???
  }

  def createOne(user: User) = ???
}
