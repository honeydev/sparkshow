package sparkshow.codecs

import io.circe._
import java.sql.Timestamp
import scala.concurrent.duration._

object CommonCodecs {

    implicit val TimestampCodecs: Encoder[Timestamp] & Decoder[Timestamp] =
        new Encoder[Timestamp] with Decoder[Timestamp] {
            override def apply(a: Timestamp): Json =
                Encoder.encodeLong.apply(a.getTime)

            override def apply(c: HCursor): Decoder.Result[Timestamp] =
                Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
        }

    implicit val FiniteDurationDecoder: Decoder[FiniteDuration] =
        Decoder.decodeLong.map(_.seconds)

    implicit val FiniteDurationEncoder: Encoder[FiniteDuration] =
        Encoder.encodeLong.contramap(_.toSeconds.toLong)
}
