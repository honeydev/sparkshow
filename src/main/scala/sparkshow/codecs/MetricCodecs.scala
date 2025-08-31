package sparkshow.codecs

import doobie.postgres.circe.jsonb.implicits.{pgDecoderGet, pgEncoderPut}
import doobie.util.meta.Meta
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sparkshow.db.models.Metric

object MetricCodecs {
    import sparkshow.codecs.CommonCodecs._

    implicit val ValueDecoder: Decoder[Metric.Value] =
        deriveDecoder[Metric.Value]
    implicit val ValueEncoder: Encoder[Metric.Value] =
        deriveEncoder[Metric.Value]

    implicit val metricEncoder: Encoder[Metric] = deriveEncoder[Metric]

    implicit val metaValueList: Meta[List[Metric.Value]] =
        new Meta[List[Metric.Value]](pgDecoderGet, pgEncoderPut)
}
