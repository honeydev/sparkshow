package sparkshow.services

import cats.effect.IO
import sparkshow.db.models.Source
import sparkshow.db.repositories.SourceRepository
import sparkshow.web.data.SourceRequestBody

class SourceService(val sourceRepository: SourceRepository) {

    def createSource(sourceRequestBody: SourceRequestBody): IO[Source] = {
       sourceRepository.insertOne(
           sourceRequestBody.name,
           sourceRequestBody.path,
           sourceRequestBody.schema
       )
    }
}
