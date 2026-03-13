package sparkshow.db.repositories

import scala.concurrent.duration._

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

    given metaListString: Meta[List[String]] =
        new Meta[List[String]](pgDecoderGet, pgEncoderPut)
    given aggregateMeta: Meta[Aggregate] =
        new Meta[Aggregate](pgDecoderGet, pgEncoderPut)
    given instantMeta: Meta[Instant] =
        Meta[Timestamp].timap(_.toInstant)(Timestamp.from)
    given periodMeta: Meta[FiniteDuration] =
        Meta[Int].timap(_.seconds)(d => d.toSeconds.toInt)

    def all: IO[List[Query]] = {
        sql"""SELECT * FROM queries"""
            .query[Query]
            .stream
            .compile
            .toList
            .transact(transactor)
    }

    def queries(
        st: Option[List[String]],
        period: Some[Unit]
    ): IO[List[(Query, Source)]] = {
        val selectClause = fr"""
             SELECT * FROM queries
             INNER JOIN sources
             ON queries.source_id = sources.id
            """
        val stateCl = st.map { states =>
            val statesFragment =
                st.map(v => fr"$v::query_state").intercalate(fr",")

            fr"state IN ($statesFragment)"
        }
        val periodCl =
            period.map { _ =>
                fr"COALESCE(EXTRACT(EPOCH FROM NOW() - last_run) > period, true)"
            }
        val whereClause = whereAndOpt(stateCl, periodCl)
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
        period: FiniteDuration,
        ownerId: Long,
        lastRun: Option[Instant] = None
    ): IO[Query] = {
        sql"""
             INSERT INTO queries (
                columns
                , grouped
                , aggregate
                , state
                , period
                , last_run
                , source_id
                , user_id
             )
             VALUES (
                $columns
                , $grouped
                , $aggregate
                , ${QueryState.`new`}::query_state
                , $period
                , $lastRun
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
              "period",
              "last_run",
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

    def update(
        state: QueryState,
        retries: Int,
        lastRun: Instant,
        id: Long
    ): IO[Int] =
        sql"""UPDATE 
          queries
        SET 
          state = ${state.toString}::query_state
          , retries = $retries
          , last_run = $lastRun
        WHERE id = $id""".update.run
            .transact(transactor)
}
