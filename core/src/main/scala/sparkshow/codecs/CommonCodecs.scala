package sparkshow.codecs

import scala.concurrent.duration._

import io.circe._
import java.sql.Timestamp

object CommonCodecs:

    given TimestampFormat: Encoder[Timestamp] & Decoder[Timestamp] =
        new Encoder[Timestamp] with Decoder[Timestamp] {
            override def apply(a: Timestamp): Json =
                Encoder.encodeLong.apply(a.getTime)

            override def apply(c: HCursor): Decoder.Result[Timestamp] =
                Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
        }

    given FiniteDurationDecoder: Decoder[FiniteDuration] =
        summon[Decoder[Long]].map(_.seconds)

    given FiniteDurationEncoder: Encoder[FiniteDuration] =
        summon[Encoder[Long]].contramap(_.toSeconds)
