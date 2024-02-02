package sparkshow.service

import cats.effect.IO
import sparkshow.db.repository.UserRepository
import sparkshow.service.UserService
import sparkshow.db.web.data.LoginRequest
import org.mindrot.jbcrypt.BCrypt

class AuthService(
    val userRepository: UserRepository
) {

    def authenticate(
        loginReq: LoginRequest
    ) = {
        for {
            targetUser <- userRepository
                .getOne(
                  loginReq.username
                )
            authenticatedUser = {
                targetUser.flatMap(u => {
                    val isValid = BCrypt
                        .checkpw(
                          loginReq.password,
                          u.passwordHash
                        )
                    if (isValid) targetUser
                    else
                        None
                })
            }
        } yield targetUser
    }
}
