@file:OptIn(ExperimentalMaterial3Api::class)

package net.domisafonov.propiotiempo.ui.content.timedactivityintervals

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.timedactivityintervals.TimedActivityIntervalsComponent
import net.domisafonov.propiotiempo.component.timedactivityintervals.TimedActivityIntervalsComponent.Command
import net.domisafonov.propiotiempo.data.formatDurationDaysHoursMinutes
import net.domisafonov.propiotiempo.data.formatInstantHoursMinutes
import net.domisafonov.propiotiempo.data.formatInstantRangeHoursMinutes
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import net.domisafonov.propiotiempo.ui.component.KeyedDropdownMenu
import net.domisafonov.propiotiempo.ui.component.KeyedDropdownMenuState
import net.domisafonov.propiotiempo.ui.component.ListItem
import net.domisafonov.propiotiempo.ui.component.rememberKeyedDropdownMenuState
import net.domisafonov.propiotiempo.ui.numericTimeBody
import net.domisafonov.propiotiempo.ui.stickyRememberBoolean
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.arrow_back
import propiotiempo.composeapp.generated.resources.menu_item_delete_interval
import propiotiempo.composeapp.generated.resources.navigate_back
import propiotiempo.composeapp.generated.resources.pending

@Immutable
data class TimedActivityIntervalsViewModel(
    val name: String,
    val intervals: List<Interval>,
) {
    @Immutable
    data class Interval(
        val activityId: Long,
        val start: Instant,
        val end: Instant,
        val isActive: Boolean,
    )
}

@Immutable
data class MenuKey(
    val activityId: Long,
    val start: Instant,
) {
    companion object {
        fun fromViewModel(
            viewModel: TimedActivityIntervalsViewModel.Interval,
        ): MenuKey = MenuKey(
            activityId = viewModel.activityId,
            start = viewModel.start,
        )
    }
}

@Composable
fun TimedActivityIntervalsContent(modifier: Modifier = Modifier, component: TimedActivityIntervalsComponent) {

    val viewModel by component.viewModel.collectAsState()

    val listState = rememberLazyListState()
    val isScrollEnabled = stickyRememberBoolean {
        listState.canScrollForward || listState.canScrollBackward
    }
    val scrollBehavior = if (isScrollEnabled) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        null
    }

    val dropdownMenuState = rememberKeyedDropdownMenuState<MenuKey>()
    LaunchedEffect(component) {
        component.commands.collect { command -> when (command) {
            is Command.ItemMenuRequest -> dropdownMenuState.requestMenu(
                key = MenuKey(
                    activityId = command.activityId,
                    start = command.intervalStart,
                ),
            )
        } }
    }

    Scaffold(
        modifier = modifier
            .run {
                scrollBehavior
                    ?.let { nestedScroll(connection = it.nestedScrollConnection) }
                    ?: this
            },
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
                .fillMaxSize()
                .consumeWindowInsets(contentPadding),
            state = listState,
            contentPadding = contentPadding,
        ) {
            items(items = viewModel.intervals, key = { it.start.epochSeconds }) { item ->
                Column(modifier = Modifier.animateItem()) {
                    IntervalItem(
                        modifier = Modifier
                            .combinedClickable(
                                onClickLabel = "TODO",
                                onLongClickLabel = "TODO",
                                onLongClick = {
                                    component.onItemLongClick(startTime = item.start)
                                },
                                onClick = {
                                    component.onItemClick(startTime = item.start)
                                },
                            )
                            .minimumInteractiveComponentSize(),
                        dropdownMenuState = dropdownMenuState,
                        viewModel = item,
                        onMenuDelete = { component.onItemDelete(startTime = item.start) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    component.dialogSlot.subscribeAsState().value.child?.instance?.let {
        when (it) {
            is TimedActivityIntervalsComponent.Dialog.EditIntervalDialog -> EditTimedActivityIntervalDialogContent(
                component = it.component,
            )
        }
    }
}

@Composable
private fun IntervalItem(
    modifier: Modifier = Modifier,
    dropdownMenuState: KeyedDropdownMenuState<MenuKey>,
    viewModel: TimedActivityIntervalsViewModel.Interval,
    onMenuDelete: () -> Unit,
) {
    ListItem(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = if (viewModel.isActive) {
                    formatInstantHoursMinutes(instant = viewModel.start)
                } else {
                    formatInstantRangeHoursMinutes(
                        start = viewModel.start,
                        end = viewModel.end,
                    )
                },
                style = MaterialTheme.typography.numericTimeBody,
            )

            if (viewModel.isActive) {
                Icon(
                    painter = painterResource(Res.drawable.pending),
                    contentDescription = "TODO",
                )
            }

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = formatDurationDaysHoursMinutes(
                    start = viewModel.start,
                    end = viewModel.end,
                ),
                style = MaterialTheme.typography.numericTimeBody,
            )

            KeyedDropdownMenu(
                state = dropdownMenuState,
                key = MenuKey.fromViewModel(viewModel),
            ) { onDismissRequest, isExpanded ->
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = onDismissRequest,
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.menu_item_delete_interval)) },
                        onClick = onMenuDelete,
                    )
                }
            }
        }
    }
}
