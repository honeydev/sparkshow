package sparkshow.conf

import pureconfig._
import pureconfig.generic.auto._

case class AppConf(
    db: DBConf
)

object AppConf {
    def load: AppConf =
        ConfigSource.default
            .loadOrThrow[AppConf]
}
