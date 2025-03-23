package sparkshow.utils

import cats.effect.{IO, Resource}
import izumi.functional.lifecycle.Lifecycle
import org.apache.spark.sql.SparkSession
import sparkshow.conf.AppConf

class SparkSessionResource(conf: AppConf) extends Lifecycle.OfCats(
  Resource.make(
    IO.blocking {
        SparkSession
            .builder
            .master("local[*]")
            .getOrCreate()
    }
  )(sparkSession => IO.blocking(sparkSession.close()))
)
