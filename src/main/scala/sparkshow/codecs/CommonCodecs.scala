package sparkshow.codecs

import io.circe._
import java.sql.Timestamp

object CommonCodecs {

    implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] =
        new Encoder[Timestamp] with Decoder[Timestamp] {
            override def apply(a: Timestamp): Json =
                Encoder.encodeLong.apply(a.getTime)

            override def apply(c: HCursor): Decoder.Result[Timestamp] =
                Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
        }
}
