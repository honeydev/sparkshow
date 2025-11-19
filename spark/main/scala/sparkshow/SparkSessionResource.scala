package sparkshow

import cats.effect.{IO, Resource}
import izumi.functional.lifecycle.Lifecycle
import org.apache.spark.sql.SparkSession

//class SparkSessionResource(conf: AppConf)
//    extends Lifecycle.OfCats(
//      Resource.make(
//        IO.blocking {
//            SparkSession.builder
//                .appName("Spark SQL basic example")
//                .master("local[*]")
//                .getOrCreate()
//        }
//      )(sparkSession => IO.blocking(sparkSession.close()))
//    )
