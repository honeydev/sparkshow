package sparkshow.service

import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.{Role, User}
import sparkshow.db.repository.{RoleRepository, UserRepository}

class UserService(
     implicit val userRepo: UserRepository,
     implicit val roleRepo: RoleRepository
) {
    def createUser(
        username: String,
        password: String,
        email: String
    ) = {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        userRepo.createOne(
          username,
          email,
          passwordHash,
            List(Role(name="ADMIN"))
        )
    }
}
