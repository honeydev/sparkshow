package sparkshow.parser
import cats.data.State
import cats.syntax.all._
import cats.Eval
import cats.data.IndexedStateT

// SELECT salary, name FROM employees GROUP BY salary
//
//
//     SELECT
// |    |         \
// salary, name   FROM 
//                |            \
//                employees    GROUP BY
//                              \
//                              salary


trait Operator

case class Select(op: Operator, columns: List[String]) extends Operator {
  val asString: String = "select"

  def parse(sql: String) {
    case class Acc(token: String, other: String, state: Eval[(CursorState, Unit)])
    // val st = State[CursorState, Unit] { 
    //   state => state match {
    //     // case ValidCharOrSpace => (ValidCharOrSpace, ())
    //     // case ValidWordEnd => (Space, ())
    //     // case Space => (ValidCharOrSpace, ())
    // }
       
   }
}

case class From(col: List[String]) extends Operator
case class GroupBy(cols: List[String]) extends Operator

trait ADT

case class Leaf(cols: List[String]) extends ADT
case class Branch(op: Operator, childs: List[ADT]) extends ADT

trait CursorState
case object StartReadOperator extends CursorState
case object ValidWordEnd extends CursorState
case object Space extends CursorState
case object Empty extends CursorState
object Parser {

  def parse(sql: String) {
    
  }
}

