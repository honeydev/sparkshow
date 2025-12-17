package sparkshow.services

import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.models.User
import sparkshow.db.repositories.UserRepository
import sparkshow.web.data.LoginRequestBody

class AuthService(
    val userRepository: UserRepository
) {

    def authenticate(loginReq: LoginRequestBody): IO[Option[User]] = {
        for {
            targetUser <- userRepository.one(loginReq.username)
            validatedUser <- IO {
                targetUser.flatMap { u =>
                    val isValid = BCrypt
                        .checkpw(
                          loginReq.password,
                          u.passwordHash
                        )
                    if (isValid) targetUser
                    else
                        None
                }
            }
        } yield validatedUser
    }
}
