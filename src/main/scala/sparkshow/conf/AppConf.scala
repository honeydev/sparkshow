package sparkshow.conf

import pureconfig._
import pureconfig.generic.auto._

case class AppConf(
    db: DBConf,
    jwt: JwtConf
)

object AppConf {
    def load: AppConf =
        ConfigSource.default
            .loadOrThrow[AppConf]
}
