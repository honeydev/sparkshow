package sparkshow.services

import sparkshow.db.repositories.MetricRepository
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.syntax._

import cats.effect.IO
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.LoggerFactory
import sparkshow.db.models.User
import sparkshow.db.repositories.UserRepository
import sparkshow.db.repositories.QueryRepository

class MetricService(
    val metricRepo: MetricRepository,
    val queryRepo: QueryRepository,
    val userRepo: UserRepository
) {

    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    implicit val logger: SelfAwareStructuredLogger[IO] =
        LoggerFactory[IO].getLogger

    def userMetrics(queryIds: List[Long], user: User) =
        metricRepo.many(queryIds)
}
