@file:UseSerializers(InstantSerializer::class)

package net.domisafonov.propiotiempo.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

@Serializable
data class TimedActivityInterval(
    val activityId: Long,
    val start: Instant,
    val end: Instant?,
)
