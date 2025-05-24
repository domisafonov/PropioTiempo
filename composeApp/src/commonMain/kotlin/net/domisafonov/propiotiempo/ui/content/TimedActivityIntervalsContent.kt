package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import net.domisafonov.propiotiempo.component.TimedActivityIntervalsComponent

data class TimedActivityIntervalsViewModel(
    val x: Int
)

@Composable
fun TimedActivityIntervalsContent(modifier: Modifier = Modifier, component: TimedActivityIntervalsComponent) {

    val viewModel by component.viewModel.collectAsState()

    Surface {
        LazyColumn(

        ) {  }
    }
}
