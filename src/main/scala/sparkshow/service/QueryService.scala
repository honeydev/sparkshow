package sparkshow.service

import sparkshow.db.model.User
import sparkshow.db.repository.QueryRepository
import sparkshow.db.web.data.QueryRequestBody

class QueryService(
    val queryRepository: QueryRepository
) {

    def createQuery(queryRequest: QueryRequestBody, user: User) = {
        queryRepository.insertOne(queryRequest.sql, user.id)
    }
}
