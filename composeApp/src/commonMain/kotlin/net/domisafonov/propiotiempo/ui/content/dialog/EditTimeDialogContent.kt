package net.domisafonov.propiotiempo.ui.content.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.component.dialog.EditTimeDialogComponent
import net.domisafonov.propiotiempo.ui.component.WheelPicker
import net.domisafonov.propiotiempo.ui.component.WheelPickerItem
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.edit_dialog_cancel
import propiotiempo.composeapp.generated.resources.edit_dialog_confirm

@Immutable
data class EditTimeDialogViewModel(
    val title: String,
    val time: LocalTime,
    val timeRange: ClosedRange<LocalTime>,
) {
    init {
        require(time in timeRange)
    }
}

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    text = viewModel.title,
                    style = MaterialTheme.typography.headlineMedium,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally,
                    ),
                ) {
                    WheelPicker(
                        modifier = Modifier,
                        items = makeTwoDigitItems(
                            start = viewModel.timeRange.start.hour,
                            end = viewModel.timeRange.endInclusive.hour,
                        ),
                        selected = viewModel.time.hour,
                        itemLines = 3.5f,
                        onSelected = component::onHourUpdate,
                    )

                    val minuteRange = makeMinuteRangeForHour(
                        timeRange = viewModel.timeRange,
                        hour = viewModel.time.hour,
                    )
                    WheelPicker(
                        modifier = Modifier,
                        items = makeTwoDigitItems(
                            start = minuteRange.start,
                            end = minuteRange.endInclusive,
                        ),
                        selected = viewModel.time.minute,
                        itemLines = 3.5f,
                        onSelected = component::onMinuteUpdate,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End,
                    ),
                ) {
                    TextButton(
                        onClick = component::onCancel,
                    ) {
                        Text(stringResource(Res.string.edit_dialog_cancel))
                    }
                    TextButton(
                        onClick = component::onConfirm,
                    ) {
                        Text(stringResource(Res.string.edit_dialog_confirm))
                    }
                }
            }
        }
    }
}

private fun makeTwoDigitItems(start: Int, end: Int): List<WheelPickerItem<Int>> {
    require(start in 0 .. 99)
    require(end in (start  .. 99))
    return (start .. end).map { i ->
        WheelPickerItem(
            id = i,
            name = i.toString().padStart(length = 2, padChar = '0'),
        )
    }
}

fun makeMinuteRangeForHour(
    timeRange: ClosedRange<LocalTime>,
    hour: Int,
): ClosedRange<Int> {
    require(hour in 0 .. 23)

    val lowMinuteBound = timeRange.start.minute
        .takeIf { hour == timeRange.start.hour } ?: 0
    val highMinuteBound = timeRange.endInclusive.minute
        .takeIf { hour == timeRange.endInclusive.hour } ?: 59

    return lowMinuteBound .. highMinuteBound
}
