package sparkshow.conf

import pureconfig._
import pureconfig.generic.derivation.default._

case class AppConf(
    db: DBConf,
    jwt: JwtConf
) derives ConfigReader

object AppConf {
    def load: AppConf =
        ConfigSource.default
            .loadOrThrow[AppConf]
}
