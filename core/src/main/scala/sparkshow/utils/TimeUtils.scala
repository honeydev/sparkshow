package sparkshow.utils

import scala.concurrent.duration.FiniteDuration

import java.time.Instant

extension (d: FiniteDuration)

    def toInstant: Instant =
        Instant.ofEpochMilli(d.toMillis)
