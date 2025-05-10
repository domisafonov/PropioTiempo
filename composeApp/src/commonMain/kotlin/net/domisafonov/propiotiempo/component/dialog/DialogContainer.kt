package net.domisafonov.propiotiempo.component.dialog

import kotlinx.datetime.LocalTime

interface DialogContainer {

    suspend fun showInfoDialog(
        title: String? = null,
        message: String,
    ): InfoResult?

    suspend fun showConfirmationDialog(
        title: String? = null,
        message: String? = null,
        okText: String? = null,
        cancelText: String? = null,
    ): ConfirmationResult?

    suspend fun showEditTimeDialog(
        title: String,
        time: LocalTime,
    ): EditTimeResult?

    sealed interface DialogResult

    enum class InfoResult : DialogResult {
        Confirmed,
        Dismissed,
    }

    enum class ConfirmationResult : DialogResult {
        Confirmed,
        Cancelled,
        Dismissed,
    }

    sealed interface EditTimeResult : DialogResult {
        data class Confirmed(val time: LocalTime) : EditTimeResult
        data object Cancelled : EditTimeResult
        data object Dismissed : EditTimeResult
    }
}
