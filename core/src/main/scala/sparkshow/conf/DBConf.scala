package sparkshow.conf

import pureconfig._
import pureconfig.generic.derivation.default._

case class DBConf(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String
) derives ConfigReader {
    def url: String =
        s"jdbc:postgresql://$host:$port/$database"
}

object DBConf {
    def load(): DBConf =
        ConfigSource.default
            .loadOrThrow[
              DBConf
            ]
}
