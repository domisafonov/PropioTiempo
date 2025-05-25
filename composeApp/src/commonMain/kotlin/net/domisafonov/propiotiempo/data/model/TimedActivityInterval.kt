package net.domisafonov.propiotiempo.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TimedActivityInterval(
    val activityId: Long,
    val start: Instant,
    val end: Instant?,
)
