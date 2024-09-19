package sparkshow.service

import sparkshow.db.repository.QueryRepository
import sparkshow.db.web.data.QueryRequestBody

class QueryService(
    val queryRepository: QueryRepository
) {

    def createQuery(queryRequest: QueryRequestBody) = {}
}
