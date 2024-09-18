package sparkshow.db.repository

import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor
import sparkshow.db.model.Query

class QueryRepository(implicit
    val transactor: Transactor[IO]
) {

    // def getAll: IO[List[Query]] = {
    //     sql"""
    //          SELECT * FROM queries"""
    //         .query[Query]
    //         .stream
    //         .compile
    //         .toList
    //         .transact(transactor)
    // }

    def insertOne(query: String, ownerId: Long): IO[Query] = {
        ???
    }
}
