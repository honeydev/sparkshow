package sparkshow.data

sealed trait QueryState

object QueryState {
    def `new`                  = New.toString
    def `failed`: String       = Failed.toString
    def `waitingRetry`: String = WaitingRetry.toString
    def `enqueued`: String     = Enqueued.toString
}

object New extends QueryState {
    override def toString: String = "new"
}

object Failed extends QueryState {
    override def toString: String = "failed"
}

object WaitingRetry extends QueryState {
    override def toString: String = "waiting_retry"
}

object Running extends QueryState {
    override def toString: String = "running"
}

object Enqueued extends QueryState {

    override def toString: String = "enqueued"
}
