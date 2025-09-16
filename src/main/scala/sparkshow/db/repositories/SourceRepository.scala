package sparkshow.db.repositories

import cats.Show
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import doobie.util.{Get, Put}
import io.circe.syntax._
import org.postgresql.util.PGobject
import sparkshow.db.models.Source.Schema
import sparkshow.db.models.{Column, Source}

import java.sql.Timestamp
import java.time.Instant
import doobie.implicits.javasql._

object SourceRepository {
    import sparkshow.db.models.Column.{encoder => colEncoder}
    implicit val instantMeta: Meta[Instant] =
        Meta[Timestamp].timap(_.toInstant)(Timestamp.from)
    implicit val showPGobject: Show[PGobject] = Show.show(_.getValue.take(250))

    implicit val get: Get[Schema] =
        Get.Advanced.other[PGobject](NonEmptyList.of("json")).temap[Schema] {
            o =>
                import io.circe.parser.decode
                import sparkshow.db.models.Column.{decoder => colDecoder}

                decode[List[Column]](o.getValue).leftMap { e =>
                    e.printStackTrace()
                    e.toString
                }
        }
    implicit val put: Put[Schema] = Put.Advanced
        .other[PGobject](NonEmptyList.of("json"))
        .tcontramap[Schema] { s =>
            val o = new PGobject
            o.setType("jsonb")
            o.setValue(s.asJson.noSpaces)
            o
        }
}

class SourceRepository(val transactor: Transactor[IO]) {
    import SourceRepository._

    def insertOne(
        name: String,
        path: String,
        header: Boolean,
        delimiter: Option[String],
        schema: List[Column]
    ): IO[Source] = {
        sql"""
            INSERT INTO sources (
                path
                , name
                , header
                , delimiter
                , schema
             )
             VALUES (
                $path
                , $name
                , $header
                , $delimiter
                , $schema
             )
           """.update
            .withUniqueGeneratedKeys[Source](
              "id",
              "created_at",
              "updated_at",
              "path",
              "name",
              "header",
              "delimiter",
              "schema"
            )
            .transact(transactor)
    }
}
