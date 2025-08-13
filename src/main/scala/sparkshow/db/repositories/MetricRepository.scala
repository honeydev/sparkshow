package sparkshow.db.repositories

import cats.effect.IO
import doobie.WeakAsync.doobieWeakAsyncForAsync
import doobie.implicits._
import cats.implicits._
import doobie.WeakAsync.doobieWeakAsyncForAsync
import doobie.postgres.circe.jsonb.implicits.{pgDecoderGet, pgEncoderPut}
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sparkshow.db.models.Metric

class MetricRepository(val transactor: Transactor[IO]) {
    import sparkshow.codecs.MetricCodecs._

    def where(queryId: Long) = {
        sql"""
             SELECT
                *
             FROM metrics
             WHERE query_id = $queryId
           """
            .query[Metric]
            .stream
            .compile
            .toList
            .transact(transactor)
    }

    def insertOne(queryId: Long, metricValue: Metric.Values): IO[Metric] = {
        sql"""
             INSERT INTO
               metrics (
                query_id,
                value
               )
               VALUES (
                $queryId
                , $metricValue
               )
             """.update
            .withUniqueGeneratedKeys[Metric](
              "id",
              "query_id",
              "value",
              "created_at",
              "updated_at"
            )
            .transact(transactor)
    }

    def many(queryIds: List[Long]): IO[List[Metric]] = {
        val idsFragment = queryIds.map(v => fr"$v").intercalate(fr",")

        fr"""
      SELECT 
        * 
      FROM 
      metrics 
      WHERE query_id IN ($idsFragment)
      """
            .query[Metric]
            .stream
            .compile
            .toList
            .transact(transactor)

    }
}
