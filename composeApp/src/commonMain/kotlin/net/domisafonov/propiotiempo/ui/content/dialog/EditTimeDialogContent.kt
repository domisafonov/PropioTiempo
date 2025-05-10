package net.domisafonov.propiotiempo.ui.content.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.component.dialog.EditTimeDialogComponent

data class EditTimeDialogViewModel(
    val title: String,
    val time: LocalTime,
)

@Composable
fun EditTimeDialogContent(component: EditTimeDialogComponent) {

    val viewModel by component.viewModel.collectAsState()

    Dialog(
        onDismissRequest = component::onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        TODO()
    }
}
