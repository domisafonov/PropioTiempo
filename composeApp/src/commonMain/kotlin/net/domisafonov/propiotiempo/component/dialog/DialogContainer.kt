package net.domisafonov.propiotiempo.component.dialog

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
}
