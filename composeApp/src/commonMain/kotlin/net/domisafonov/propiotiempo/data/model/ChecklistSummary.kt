package net.domisafonov.propiotiempo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistSummary(
    val id: Long,
    val name: String,
    val isCompleted: Boolean,
)
