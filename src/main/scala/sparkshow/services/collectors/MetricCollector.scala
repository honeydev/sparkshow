package sparkshow.services.collectors

import sparkshow.db.models.{Query, Source}

trait MetricCollector[F[_]] {

    def collect(query: Query, source: Source): F[_]
}
