package net.domisafonov.propiotiempo.ui.content.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.domisafonov.propiotiempo.component.dialog.ConfirmationDialogComponent
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.default_confirmation_dialog_cancel
import propiotiempo.composeapp.generated.resources.default_confirmation_dialog_confirm
import propiotiempo.composeapp.generated.resources.default_confirmation_dialog_message

data class ConfirmationDialogViewModel(
    val title: String?,
    val message: String?,
    val okText: String?,
    val cancelText: String?,
)

@Composable
fun ConfirmationDialogContent(component: ConfirmationDialogComponent) {

    val viewModel by component.viewModel.collectAsState()

    val (title, message) = when {
        viewModel.title == null && viewModel.message == null ->
            null to stringResource(Res.string.default_confirmation_dialog_message)
        else -> viewModel.title to viewModel.message
    }

    AlertDialog(
        onDismissRequest = component::onDismiss,
        confirmButton = {
            TextButton(
                onClick = component::onConfirm,
            ) {
                Text(
                    text = viewModel.okText
                        ?: stringResource(
                            Res.string.default_confirmation_dialog_confirm,
                        ),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = component::onCancel,
            ) {
                Text(
                    text = viewModel.cancelText
                        ?: stringResource(
                            Res.string.default_confirmation_dialog_cancel,
                        ),
                )
            }
        },
        title = title?.let {{ Text(it) }},
        text = message?.let {{ Text(it) }},
    )
}
