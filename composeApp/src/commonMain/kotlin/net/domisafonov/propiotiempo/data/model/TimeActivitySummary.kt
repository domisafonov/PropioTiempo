package net.domisafonov.propiotiempo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimeActivitySummary(
    val id: Long,
    val name: String,
    val todaysSeconds: Long,
    val isActive: Boolean,
)
