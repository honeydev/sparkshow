package sparkshow.conf

import pureconfig._
import pureconfig.generic.auto._

case class DBConf(
                 host: String,
                 port: Int,
                 database: String,
                 username: String,
                 password: String
                 ) {
  def url =
    s"jdbc:postgresql://$host:$port/$database"
}

object DBConf {
  def load() =
    ConfigSource.default.loadOrThrow[DBConf]
}