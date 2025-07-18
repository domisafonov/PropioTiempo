package net.domisafonov.propiotiempo.component.dialog

import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.data.dayEnd
import net.domisafonov.propiotiempo.data.dayStart
import org.jetbrains.compose.resources.getString
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.default_error_message
import propiotiempo.composeapp.generated.resources.default_error_title

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
        timeRange: ClosedRange<LocalTime>,
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

suspend fun DialogContainer.showErrorDialog(
    title: String? = null,
    message: String? = null,
): DialogContainer.InfoResult? {

    require(title == null || message != null)

    val title = title
        ?: if (message == null) {
            null
        } else {
            getString(Res.string.default_error_title)
        }
    val message = message ?: getString(Res.string.default_error_message)

    return showInfoDialog(
        title = title,
        message = message,
    )
}

suspend fun DialogContainer.showEditTimeDialog(
    title: String,
    time: LocalTime,
    onlyLaterThanOrEqual: LocalTime? = null,
    onlyEarlierThanOrEqual: LocalTime? = null,
): DialogContainer.EditTimeResult? {
    require(
        onlyLaterThanOrEqual == null ||
            onlyEarlierThanOrEqual == null ||
            onlyLaterThanOrEqual <= onlyEarlierThanOrEqual
    )
    return showEditTimeDialog(
        title = title,
        time = time,
        timeRange = (onlyLaterThanOrEqual ?: LocalTime.dayStart()) ..
            (onlyEarlierThanOrEqual ?: LocalTime.dayEnd()),
    )
}
