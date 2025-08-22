package sparkshow.db.repositories

import cats.implicits._
import doobie.implicits._
import doobie.util.fragment.Fragment

/** Doobie fragment utils and other common functions.
  */
trait SQLOps {

    /** SQL fragment IN (1, 2, 3, 4)
      */
    def longInClause(ids: List[Long]): Fragment =
        fr"(" ++ ids.map(v => fr"$v").intercalate(fr",") ++ fr")"
}
