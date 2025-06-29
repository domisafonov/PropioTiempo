package net.domisafonov.propiotiempo.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

@Serializable
data class LimitedLocalTimedActivityInterval(
    val activityId: Long,
    val timeZone: TimeZone,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val lowerEditLimit: LocalDateTime?,
    val upperEditLimit: LocalDateTime?,
)
