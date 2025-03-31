package sparkshow.db.models

sealed trait Type
case object Numeric extends Type
case object String extends Type

case class Column(name: String, `type`: String)
case class Source(name: String, schema: List[Type])
