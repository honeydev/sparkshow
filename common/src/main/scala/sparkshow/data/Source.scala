package sparkshow.data

sealed trait Type
case object NumericT extends Type
case object StringT extends Type

abstract class BaseColumn(val name: String, val `type`: Type)
