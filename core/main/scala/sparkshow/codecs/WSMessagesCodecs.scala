package sparkshow.codecs

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sparkshow.web.data.GetMetrics

object WSMessagesCodecs {

    implicit val getMertricsDecoder: Decoder[GetMetrics] =
        deriveDecoder[GetMetrics]
}
