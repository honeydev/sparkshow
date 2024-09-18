package sparkshow.db.model


//import doobie.postgres._
//import doobie.postgres.implicits._


case class Query(
    id: Long,
    query: String,
    state: String,
)


//object Query {
//  implicit val MyJavaEnumMeta = pgJavaEnum[QueryStates]("myenum")
//}
//
