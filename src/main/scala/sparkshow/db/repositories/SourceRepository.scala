package sparkshow.db.repositories

import cats.effect.IO
import doobie.util.transactor.Transactor
import sparkshow.db.models.Source.Schema
import sparkshow.db.models.{Aggregate, Column, Source}
import doobie.implicits._
import doobie.postgres.circe.jsonb.implicits.{pgDecoderGet, pgEncoderPut}
import doobie.util.meta.Meta
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sparkshow.web.data.SourceRequestBody

class SourceRepository(val transactor: Transactor[IO]) {
    import sparkshow.db.models.Source._

    implicit val metaSchema: Meta[Schema] = new Meta[Schema](pgDecoderGet, pgEncoderPut)
    implicit val meta: Meta[Source] = new Meta[Source](pgDecoderGet, pgEncoderPut)

    def insertOne(name: String, path: String, schema: Schema) = {
        sql"""
            INSERT INTO sources (
                name
                , path
                , schema
             )
             VALUES (
                $name
                , $path
                , $schema
             )
           """
            .update
            .withUniqueGeneratedKeys[Source](
              "id",
              "name",
              "path",
              "schema"
            )
            .transact(transactor)
    }
}
