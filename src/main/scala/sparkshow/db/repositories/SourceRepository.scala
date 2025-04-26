package sparkshow.db.repositories

import cats.Show
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.{Get, Put}
import io.circe.syntax._
import org.postgresql.util.PGobject
import sparkshow.db.models.Source.Schema
import sparkshow.db.models.{Column, Source}

class SourceRepository(val transactor: Transactor[IO]) {
    import sparkshow.db.models.Column.{encoder => colEncoder}

    implicit val showPGobject: Show[PGobject] = Show.show(_.getValue.take(250))

    implicit val get: Get[Schema] = Get.Advanced.other[PGobject](NonEmptyList.of("json")).temap[Schema] { o =>
        import io.circe.parser.decode
        import sparkshow.db.models.Column.{decoder => colDecoder}

        decode[List[Column]](o.getValue).leftMap { e =>
            e.printStackTrace()
            e.toString
        }
    }
    implicit val put: Put[Schema] = Put.Advanced.other[PGobject](NonEmptyList.of("json")).tcontramap[Schema] { s =>
        val o = new PGobject
        o.setType("jsonb")
        o.setValue(s.asJson.noSpaces)
        o
    }

    def insertOne(name: String, path: String, schema: List[Column]): IO[Source] = {
        sql"""
            INSERT INTO sources (
                path
                , name
                , schema
             )
             VALUES (
                $path
                , $name
                , $schema
             )
           """
            .update
            .withUniqueGeneratedKeys[Source](
              "id",
              "path",
              "name",
              "schema"
            )
            .transact(transactor)
    }
}
