@file:OptIn(ExperimentalMaterial3Api::class)

package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.TimedActivityIntervalsComponent
import net.domisafonov.propiotiempo.data.formatInstantHoursMinutes
import net.domisafonov.propiotiempo.data.formatInstantRangeHoursMinutes
import net.domisafonov.propiotiempo.ui.component.ListItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.arrow_back
import propiotiempo.composeapp.generated.resources.navigate_back

data class TimedActivityIntervalsViewModel(
    val name: String,
    val intervals: List<Interval>,
) {
    data class Interval(
        val activityId: Long,
        val start: Instant,
        val end: Instant?,
    )
}

@Composable
fun TimedActivityIntervalsContent(modifier: Modifier = Modifier, component: TimedActivityIntervalsComponent) {

    val viewModel by component.viewModel.collectAsState()

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        canScroll = { listState.canScrollForward || listState.canScrollBackward }
    )

    Scaffold(
        modifier = modifier
            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = component::onNavigateBack,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.navigate_back),
                        )
                    }
                },
                title = { Text(viewModel.name) },
                windowInsets = TopAppBarDefaults.windowInsets,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
        ) {
            items(items = viewModel.intervals, key = { it.start.epochSeconds }) { item ->
                IntervalItem(viewModel = item)
            }
        }
    }
}

@Composable
private fun IntervalItem(
    modifier: Modifier = Modifier,
    viewModel: TimedActivityIntervalsViewModel.Interval,
) {
    ListItem(
        modifier = modifier,
    ) {
        Row {
            Text(
                text = if (viewModel.end == null) {
                    formatInstantHoursMinutes(instant = viewModel.start)
                } else {
                    formatInstantRangeHoursMinutes(
                        start = viewModel.start,
                        end = viewModel.end,
                    )
                },
            )

            // TODO
        }
    }
}
