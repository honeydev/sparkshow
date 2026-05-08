package sparkshow.codecs

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sparkshow.web.data.GetMetrics
import sparkshow.web.data.MetricRequest

object WSMessagesCodecs {

    given Decoder[MetricRequest] = deriveDecoder[MetricRequest]

    given getMetricDecoder: Decoder[GetMetrics] =
        deriveDecoder[GetMetrics]
}
