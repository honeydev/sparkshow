package sparkshow.utils

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

extension (d: FiniteDuration)
    
    def toInstant: Instant =
        Instant.ofEpochMilli(d.toMillis)
