package sparkshow.data

sealed trait Function

case object Sum extends Function {
    override def toString = "sum"
}

case object Count extends Function {
    override def toString = "count"
}

case class Aggregate(column: String, function: Function)
