package sparkshow.service

import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt
import sparkshow.db.model.User
import sparkshow.db.repository.UserRepository
import sparkshow.db.web.data.LoginRequest
import sparkshow.db.repository.QueryRepository
import sparkshow.db.web.data.QueryRequest

class QueryService(
    val queryRepository: QueryRepository
) {

  def createQuery(queryRequest: QueryRequest) = {
    
  }
}

