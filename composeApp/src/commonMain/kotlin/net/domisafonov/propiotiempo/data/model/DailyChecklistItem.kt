package net.domisafonov.propiotiempo.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DailyChecklistItem(
    val id: Long,
    val name: String?,
    val checkedTime: Instant?,
)
