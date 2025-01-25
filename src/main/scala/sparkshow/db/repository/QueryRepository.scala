package sparkshow.db.repository

import cats.effect._
import doobie.Read
import doobie.implicits._
import doobie.util.transactor.Transactor
import sparkshow.db.model.{Query, QueryState}

class QueryRepository(implicit
    val transactor: Transactor[IO]
) {

//    implicit val read: Read[Query] = {}

//    def all: IO[List[Query]] = {
//        sql"""
//             SELECT * FROM queries"""
//            .query[Query]
//            .stream
//            .compile
//            .toList
//            .transact(transactor)
//    }

    def insertOne(query: String, ownerId: Long)(implicit
        read: Read[Query]
    ): IO[Query] = {
        sql"""
             INSERT INTO queries (
                query
                , state
                , owner_id
                , user_id
             )
             VALUES ($query, ${QueryState.`new`}, $ownerId)
           """.update
            .withUniqueGeneratedKeys[Query](
              "id",
              "query",
              "state",
              "user_id"
            )
            .transact(transactor)
    }
}
