package net.domisafonov.propiotiempo.ui.content.timedactivityintervals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import net.domisafonov.propiotiempo.component.timedactivityintervals.EditTimedActivityIntervalDialogComponent

@Immutable
sealed interface EditTimedActivityIntervalDialogViewModel {

    data object Initializing : EditTimedActivityIntervalDialogViewModel

    data class Ready(
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val timeRange: ClosedRange<LocalDateTime>,
    ) : EditTimedActivityIntervalDialogViewModel
}

@Composable
fun EditTimedActivityIntervalDialogContent(component: EditTimedActivityIntervalDialogComponent) {

    val viewModel by component.viewModel.collectAsState()

    Dialog(
        onDismissRequest = component::onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        val snackbarState = remember { SnackbarHostState() }

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Box {
                val sc = rememberCoroutineScope()
                Text("AAA", modifier = Modifier.height(200.dp).clickable { sc.launch { snackbarState.showSnackbar(message = "BBB") } })
                SnackbarHost(
                    hostState = snackbarState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                )
            }
        }
    }
}
