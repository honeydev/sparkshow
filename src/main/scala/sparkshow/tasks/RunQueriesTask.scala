package sparkshow.tasks

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{
    LongType,
    StringType,
    StructField,
    StructType
}
import sparkshow.db.models._
import sparkshow.db.repositories.{MetricRepository, QueryRepository}

import scala.concurrent.duration._
import sparkshow.services.QueryQueueService
import cats.effect.ExitCode

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

//     def run: IO[Unit] = {
//         def loop(queue: IO[Queue[IO, (Query, Source)]]): IO[Unit] = {
//             val nextQ = {
//                 for {
//                     q <- queue
//                     // FIXME take only not enqueued queries from db!
//                     queries <- queryRepository.queries(
//                       List(New.toString, WaitingRetry.toString)
//                     )
//                     _         <- q.tryOfferN(queries)
//                     extracted <- q.tryTakeN(Some(10))
//                     _         <- IO.println(extracted)
//                     updated <- {
//                         extracted.parTraverse {
//                             case (q, s) => {
//                                 val calculate = IO.blocking {
//                                     val schema =
//                                         StructType(s.schema.map { col =>
//                                             val sparkT = col.`type` match {
//                                                 case StringT  => StringType
//                                                 case NumericT => LongType
//                                             }
//                                             StructField(col.name, sparkT)
//                                         })
//
//                                     val df = sparkSession.read
//                                         .option("header", "true")
//                                         .option("delimiter", ";")
//                                         .schema(schema)
//                                         // TODO impl diff file sources.
//                                         .csv(s.path)
//
//                                     val grouped =
//                                         df.groupBy(q.grouped.map(col): _*)
//
//                                     val aggregated =
//                                         (q.aggregate.function match {
//                                             case Sum =>
//                                                 grouped.agg(
//                                                   sum(q.aggregate.column)
//                                                       .alias(q.aggregate.column)
//                                                 )
//                                             case Count =>
//                                                 grouped.agg(
//                                                   count(
//                                                     col(q.aggregate.column)
//                                                         .alias(
//                                                           q.aggregate.column
//                                                         )
//                                                   )
//                                                 )
//                                         })
//
//                                     aggregated
//                                         .collect()
//                                         .map(r =>
//                                             Metric.Value(
//                                               label = q.grouped.mkString(", "),
//                                               name = q.grouped
//                                                   .map(r.getAs[String](_))
//                                                   .mkString(", "),
//                                               value = r.getAs[Long](
//                                                 q.aggregate.column
//                                               )
//                                             )
//                                         )
//                                         .toList
//                                 }
//
//                                 val res = for {
//                                     _ <- queryRepository.update(Running, q.id)
//                                     metricData <- calculate
//                                     m <- metricRepository.insertOne(
//                                       q.id,
//                                       metricData
//                                     )
//                                     _ <- IO.println("Metric id: ", m)
//                                     _ <- queryRepository.update(
//                                       WaitingRetry,
//                                       retries = 0,
//                                       q.id
//                                     )
//                                 } yield ()
//
//                                 res.handleErrorWith { e =>
//                                     for {
//                                         _ <-
//                                             // TODO Move to const
//                                             if (q.retries > 3) {
//                                                 queryRepository.update(
//                                                   Failed,
//                                                   q.id
//                                                 )
//                                             } else {
//                                                 queryRepository.update(
//                                                   state   = WaitingRetry,
//                                                   retries = q.retries + 1,
//                                                   id      = q.id
//                                                 )
//                                             }
//                                         _ <- IO.println("Raised:", e)
//                                     } yield ()
//                                 }
//                             }
//                         }
//                     }
//                     _ <- IO.println(updated)
//                 } yield (q)
//             }
//             nextQ >> IO.println("DELAY END") >> IO
//                 .defer { loop(nextQ) }
//                 .delayBy(300.seconds)
//         }
//         loop(Queue.bounded[IO, (Query, Source)](50))
//     }
}
