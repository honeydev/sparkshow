package sparkshow.service

import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.User
import sparkshow.db.repository.UserRepository

class UserService(userRepo: UserRepository) {
  def createUser(username: String, password: String): User = {
    val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
    val newUser = User(username=username, passwordHash=passwordHash)
    userRepo.createOne(newUser)
    ???
  }
}
