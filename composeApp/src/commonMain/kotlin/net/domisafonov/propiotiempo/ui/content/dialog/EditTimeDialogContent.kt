package net.domisafonov.propiotiempo.ui.content.dialog

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.component.dialog.EditTimeDialogComponent
import net.domisafonov.propiotiempo.ui.component.WheelPicker
import net.domisafonov.propiotiempo.ui.component.WheelPickerItem

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
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            WheelPicker(
                modifier = Modifier,
                items = listOf(
                    WheelPickerItem(1, "1"),
                    WheelPickerItem(2, "2"),
                    WheelPickerItem(3, "3"),
                    WheelPickerItem(4, "4"),
                    WheelPickerItem(5, "5"),
                    WheelPickerItem(6, "6"),
                    WheelPickerItem(7, "7"),
                    WheelPickerItem(8, "8"),
                    WheelPickerItem(9, "9"),
                    WheelPickerItem(10, "10"),
                    WheelPickerItem(11, "11"),
                    WheelPickerItem(12, "12"),
                    WheelPickerItem(13, "13"),
                    WheelPickerItem(14, "14"),
                    WheelPickerItem(15, "15"),
                    WheelPickerItem(16, "16"),
                    WheelPickerItem(17, "17"),
                    WheelPickerItem(18, "18"),
                ),
                selected = 5,
                itemLines = 4.4f,
                onSelected = { println("slctd: $it") },
            )
        }
    }
}
