package net.domisafonov.propiotiempo.data.model

import kotlin.time.Instant

data class LimitedTimedActivityInterval(
    val activityId: Long,
    val start: Instant,
    val end: Instant?,
    val lowerEditLimit: Instant?,
    val upperEditLimit: Instant?,
)
