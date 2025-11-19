package sparkshow.codecs

import doobie.postgres.circe.jsonb.implicits.{pgDecoderGet, pgEncoderPut}
import doobie.util.meta.Meta
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sparkshow.data.MetricValue
import sparkshow.db.models.Metric

object MetricCodecs {
    import sparkshow.codecs.CommonCodecs._

    implicit val ValueDecoder: Decoder[MetricValue] =
        deriveDecoder[MetricValue]
    implicit val ValueEncoder: Encoder[MetricValue] =
        deriveEncoder[MetricValue]

    implicit val metricEncoder: Encoder[Metric] = deriveEncoder[Metric]

    implicit val metaValueList: Meta[List[MetricValue]] =
        new Meta[List[MetricValue]](pgDecoderGet, pgEncoderPut)
}
