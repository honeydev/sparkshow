package sparkshow.web.data

trait IncomeMsg
case class GetMetrics(queries: List[Long]) extends IncomeMsg

sealed trait SendState

case class SendMetrics(incomeMsg: GetMetrics) extends SendState
case class SendNothing() extends SendState

abstract class MessageWrapper(val message: IncomeMsg) {
    def state: SendState
}
