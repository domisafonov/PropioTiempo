@file:UseSerializers(InstantSerializer::class)

package net.domisafonov.propiotiempo.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

@Serializable
data class DailyChecklistItem(
    val id: Long,
    val name: String?,
    val checkedTime: Instant?,
)
