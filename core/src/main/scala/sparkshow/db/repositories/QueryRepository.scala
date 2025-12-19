package sparkshow.db.repositories

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie.WeakAsync.doobieWeakAsyncForAsync
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.circe.jsonb.implicits.pgDecoderGet
import doobie.postgres.circe.jsonb.implicits.pgEncoderPut
import doobie.util.fragments.whereAndOpt
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import java.sql.Timestamp
import java.time.Instant
import sparkshow.data.Aggregate
import sparkshow.data.QueryState
import sparkshow.db.models.Query
import sparkshow.db.models.Source

class QueryRepository(private val transactor: Transactor[IO]) extends SQLOps {
    import SourceRepository.get
    import sparkshow.db.models.Aggregate.{decoder, encoder}

    implicit val metaListString: Meta[List[String]] =
        new Meta[List[String]](pgDecoderGet, pgEncoderPut)
    implicit val aggregateMeta: Meta[Aggregate] =
        new Meta[Aggregate](pgDecoderGet, pgEncoderPut)
    implicit val instantMeta: Meta[Instant] =
        Meta[Timestamp].timap(_.toInstant)(Timestamp.from)

    def all: IO[List[Query]] = {
        sql"""SELECT * FROM queries"""
            .query[Query]
            .stream
            .compile
            .toList
            .transact(transactor)
    }

    def queries(st: List[String]): IO[List[(Query, Source)]] = {
        val states = st.map(v => fr"$v::query_state").intercalate(fr",")
        val selectClause = fr"""
             SELECT * FROM queries
             INNER JOIN sources
             ON queries.source_id = sources.id
            """
        val stateCl     = fr"state IN ($states)"
        val whereClause = whereAndOpt(Some(stateCl), Some(stateCl))
        (selectClause ++ whereClause)
            .query[(Query, Source)]
            .stream
            .compile
            .toList
            .transact(transactor)
    }

    def insertOne(
        sourceId: Long,
        columns: List[String],
        grouped: List[String],
        aggregate: Aggregate,
        ownerId: Long
    ): IO[Query] = {
        sql"""
             INSERT INTO queries (
                columns
                , grouped
                , aggregate
                , state
                , source_id
                , user_id
             )
             VALUES (
                $columns
                , $grouped
                , $aggregate
                , ${QueryState.`new`}::query_state
                , $sourceId
                , $ownerId
             )
           """.update
            .withUniqueGeneratedKeys[Query](
              "id",
              "user_id",
              "source_id",
              "columns",
              "grouped",
              "aggregate",
              "state",
              "retries",
              "created_at",
              "updated_at"
            )
            .transact(transactor)
    }

    def update(state: QueryState, id: Long): IO[Int] = {
        sql"""UPDATE queries SET state = ${state.toString}::query_state WHERE id = $id""".update.run
            .transact(transactor)
    }

    def update(state: QueryState, ids: NonEmptyList[Long]): IO[Int] = {
        val idsFr = longInClause(ids)
        sql"""UPDATE queries SET state = ${state.toString}::query_state WHERE id IN $idsFr""".update.run
            .transact(transactor)
    }

    def update(state: QueryState, retries: Int, id: Long): IO[Int] =
        sql"""UPDATE queries SET state = ${state.toString}::query_state, retries = $retries WHERE id = $id""".update.run
            .transact(transactor)
}
