package sparkshow.service

import cats.data.EitherT
import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.{Role, User}
import sparkshow.db.repository.{RoleRepository, UserRepository}

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
            .fromOptionF(userRepo.one(id), s"User with id $id is not found")

}
