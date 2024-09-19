package sparkshow.service

import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.Role
import sparkshow.db.model.User
import sparkshow.db.repository.RoleRepository
import sparkshow.db.repository.UserRepository
import cats.data.EitherT

class UserService(val userRepo: UserRepository, val roleRepo: RoleRepository) {
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

    def findUser(id: Long): EitherT[IO, String, User] =
        EitherT
            .fromOptionF(userRepo.getOne(id), s"User with id $id is not found")

}
