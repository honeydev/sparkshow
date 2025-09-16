package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import org.apache.spark.sql.SparkSession
import sparkshow.db.models._
import sparkshow.db.repositories.{MetricRepository, QueryRepository}

import sparkshow.services.QueryQueueService

class RunQueriesTask(
    val queryRepository: QueryRepository,
    val metricRepository: MetricRepository,
    val sparkSession: SparkSession,
    val queryQueueService: QueryQueueService
) {

    def run: IO[Unit] = {

        Queue.bounded[IO, (Query, Source)](50).flatMap { q =>
            val producerFiber =
                queryQueueService.produceQueries(q).foreverM.start
            val consumerFiber =
                queryQueueService.handleQueries(q).foreverM.start
            val paired = (producerFiber, consumerFiber).parTupled.void
            paired
        }
    }
}
