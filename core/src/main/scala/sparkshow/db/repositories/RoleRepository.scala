package sparkshow.db.repositories

import cats.effect._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import java.sql.Timestamp
import java.time.Instant
import sparkshow.db.models.Role

class RoleRepository(implicit
    private val transactor: Transactor[IO]
) {
    implicit val instantMeta: Meta[Instant] =
        Meta[Timestamp].timap(_.toInstant)(Timestamp.from)
    def many(userId: Long): IO[List[Role]] = {
        sql"""
             SELECT
                roles.id,
                roles.created_at,
                roles.updated_at,
                name
             FROM roles
             LEFT JOIN users_roles ON roles.id = users_roles.role_id
             WHERE user_id = ${userId}"""
            .query[Role]
            .stream
            .compile
            .toList
            .transact(transactor)
    }
}
