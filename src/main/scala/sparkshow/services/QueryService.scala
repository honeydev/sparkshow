package sparkshow.services

import cats.effect.IO
import sparkshow.db.models.{Query, User}
import sparkshow.db.repositories.QueryRepository
import sparkshow.web.data.QueryRequestBody

class QueryService(
    val queryRepository: QueryRepository
) {

    def createQuery(queryRequest: QueryRequestBody, user: User): IO[Query] = {
        queryRepository.insertOne(
          queryRequest.columns,
          queryRequest.grouped,
          queryRequest.aggregate,
          queryRequest.source_path,
          user.id
        )
    }
}
