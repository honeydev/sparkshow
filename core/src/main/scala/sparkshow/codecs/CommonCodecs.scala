package sparkshow.codecs

import io.circe.*
import java.sql.Timestamp
import scala.concurrent.duration.*

object CommonCodecs:

    given TimestampCodecs: Encoder[Timestamp] & Decoder[Timestamp] with
        def apply(a: Timestamp): Json = Encoder.encodeLong(a.getTime)
        def apply(c: HCursor): Decoder.Result[Timestamp] =
            summon[Decoder[Long]].map(Timestamp(_))(c)

    given FiniteDurationDecoder: Decoder[FiniteDuration] =
        summon[Decoder[Long]].map(_.seconds)

    given FiniteDurationEncoder: Encoder[FiniteDuration] =
        summon[Encoder[Long]].contramap(_.toSeconds)
