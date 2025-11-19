package sparkshow.db.models

import sparkshow.data.Aggregate
import sparkshow.services.QueryProperties

import java.time.Instant

case class Query(
    id: Long,
    userId: Long,
    sourceId: Long,
    createdAt: Instant,
    updatedAt: Instant,
    columns: List[String],
    grouped: List[String],
    aggregate: Aggregate,
    state: String,
    retries: Int = 0
) {
    def toProps =
        QueryProperties(
            this.id,
            this.userId,
            this.sourceId,
            this.createdAt,
            this.updatedAt,
            this.columns,
            this.grouped,
            this.aggregate,
            this.state,
            this.retries
        )
}
