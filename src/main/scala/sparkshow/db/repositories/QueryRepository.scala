package sparkshow.db.repositories

import cats.effect._
import doobie.implicits._
import doobie.postgres.circe.jsonb.implicits.{pgDecoderGet, pgEncoderPut}
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import sparkshow.db.models.{Aggregate, Query, QueryState}


class QueryRepository(val transactor: Transactor[IO]) {
    import Aggregate.{decoder, encoder}

    implicit val metaList: Meta[List[String]] = new Meta[List[String]](pgDecoderGet, pgEncoderPut)
    implicit val meta: Meta[Aggregate] = new Meta[Aggregate](pgDecoderGet, pgEncoderPut)

    def all: IO[List[Query]] = {
        sql"""SELECT * FROM queries"""
            .query[Query]
            .stream
            .compile
            .toList
            .transact(transactor)
    }

    def newQueries: IO[List[Query]] =
        sql"""SELECT * FROM queries WHERE state = ${QueryState.`new`}::query_state"""
            .query[Query]
            .stream
            .compile
            .toList
            .transact(transactor)

    def insertOne(
                   columns: List[String],
                   grouped: List[String],
                   aggregate: Aggregate,
                   sourcePath: String,
                   ownerId: Long
                 ): IO[Query] = {
        sql"""
             INSERT INTO queries (
                columns
                , grouped
                , aggregate
                , state
                , source_path
                , user_id
             )
             VALUES (
                $columns
                , $grouped
                , $aggregate
                , ${QueryState.`new`}::query_state
                , $sourcePath
                , $ownerId
             )
           """.update
            .withUniqueGeneratedKeys[Query](
              "id",
              "user_id",
              "columns",
              "grouped",
              "aggregate",
              "state",
              "source_path",
              "retries",
              "created_at",
              "updated_at"
            )
            .transact(transactor)
    }
}
