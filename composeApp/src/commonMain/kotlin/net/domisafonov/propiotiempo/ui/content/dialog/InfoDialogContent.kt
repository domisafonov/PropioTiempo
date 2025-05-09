package net.domisafonov.propiotiempo.ui.content.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.domisafonov.propiotiempo.component.dialog.InfoDialogComponent
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.default_confirmation_dialog_confirm

data class InfoDialogViewModel(
    val title: String?,
    val message: String,
)

@Composable
fun InfoDialogContent(component: InfoDialogComponent) {

    val viewModel by component.viewModel.collectAsState()

    val (title, message) = viewModel.title
        ?.let { it to viewModel.message }
        ?: (viewModel.message to null)

    AlertDialog(
        onDismissRequest = component::onDismiss,
        confirmButton = {
            TextButton(
                onClick = component::onOk,
            ) {
                Text(stringResource(Res.string.default_confirmation_dialog_confirm))
            }
        },
        title = { Text(title) },
        text = message?.let {{ Text(it) }},
    )
}
