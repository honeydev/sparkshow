package sparkshow.service

import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.Role
import sparkshow.db.repository.RoleRepository
import sparkshow.db.repository.UserRepository
import cats.effect.IO
import sparkshow.db.model.User

class UserService(
    implicit val userRepo: UserRepository,
    implicit val roleRepo: RoleRepository
) {
    def createUser(
        username: String,
        password: String,
        email: String
    ): IO[User] = {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        userRepo.createOne(
          username,
          email,
          passwordHash,
          List(Role(name = "ADMIN"))
        )
    }
}
