package sparkshow.web.data

import java.time.Instant

trait IncomeMsg
case class GetMetrics(metrics: List[MetricRequest]) extends IncomeMsg

sealed trait SendState

case class SendMetrics(incomeMsg: GetMetrics) extends SendState
case class SendNothing() extends SendState

abstract class MessageWrapper(val message: IncomeMsg) {
    def state: SendState
}

case class MetricRequest(
    queryId: Long,
    start: Option[Instant],
    end: Option[Instant]
)
