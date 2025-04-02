package sparkshow.web

import cats.effect.IO
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto._
import org.http4s.circe.jsonOf

package object data {

    implicit val createQueryEncoder: Encoder[CreateQueryResponse] = deriveEncoder
    implicit val customConfig: Configuration =
        Configuration.default.withSnakeCaseMemberNames

}
