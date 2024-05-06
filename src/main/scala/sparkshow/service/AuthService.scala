package sparkshow.service

import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.User
import sparkshow.db.repository.UserRepository
import sparkshow.db.web.data.LoginRequest

class AuthService(
    val userRepository: UserRepository
) {

    def authenticate(
        loginReq: LoginRequest
                    ): IO[Option[User]] = {

        for {
            targetUser <- userRepository.getOne(loginReq.username)
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
