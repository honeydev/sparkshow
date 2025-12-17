package sparkshow.services

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sparkshow.db.models.User
import sparkshow.db.repositories.MetricRepository
import sparkshow.db.repositories.QueryRepository
import sparkshow.db.repositories.UserRepository

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
