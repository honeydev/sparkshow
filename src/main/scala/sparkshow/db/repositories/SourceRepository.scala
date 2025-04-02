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

    implicit val meta: Meta[Schema] = new Meta[Column](pgDecoderGet, pgEncoderPut)

    def insertOne(name: String, path: String, schema: List[Column]) = {
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
