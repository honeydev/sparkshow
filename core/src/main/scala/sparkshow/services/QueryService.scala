package sparkshow.services

import cats.effect.IO
import sparkshow.db.models.Query
import sparkshow.db.models.User
import sparkshow.db.repositories.QueryRepository
import sparkshow.web.data.QueryRequestBody

class QueryService(
    val queryRepository: QueryRepository
) {

    def createQuery(queryRequest: QueryRequestBody, user: User): IO[Query] = {
        queryRepository.insertOne(
          queryRequest.sourceId,
          queryRequest.columns,
          queryRequest.grouped,
          queryRequest.aggregate,
          user.id
        )
    }

}
