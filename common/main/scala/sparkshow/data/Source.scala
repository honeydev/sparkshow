package sparkshow.data

sealed trait Type
case object NumericT extends Type
case object StringT extends Type

case class Column(name: String, `type`: Type)
