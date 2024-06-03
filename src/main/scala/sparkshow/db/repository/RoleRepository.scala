package sparkshow.db.repository

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import sparkshow.db.model.Role

class RoleRepository(implicit
    val transactor: HikariTransactor[IO]
) {
    private val TableName = "roles"

    def getMany(userId: Long): IO[List[Role]] = {
        sql"""
             SELECT
                roles.id,
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
