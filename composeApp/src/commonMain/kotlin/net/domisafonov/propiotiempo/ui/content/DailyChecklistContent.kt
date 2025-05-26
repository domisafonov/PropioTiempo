@file:OptIn(ExperimentalMaterial3Api::class)

package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.DailyChecklistComponent
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import net.domisafonov.propiotiempo.ui.component.HourMinuteText
import net.domisafonov.propiotiempo.ui.component.ListItem
import net.domisafonov.propiotiempo.ui.unfilledBody
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.arrow_back
import propiotiempo.composeapp.generated.resources.check_circle
import propiotiempo.composeapp.generated.resources.daily_checklist_item_check
import propiotiempo.composeapp.generated.resources.daily_checklist_item_complete
import propiotiempo.composeapp.generated.resources.daily_checklist_item_ordinal
import propiotiempo.composeapp.generated.resources.daily_checklist_item_pending
import propiotiempo.composeapp.generated.resources.daily_checklist_item_uncheck
import propiotiempo.composeapp.generated.resources.navigate_back
import propiotiempo.composeapp.generated.resources.pending

data class DailyChecklistViewModel(
    val name: String,
    val items: List<Item>,
) {
    data class Item(
        val id: Long,
        val name: String?,
        val checkedTime: Instant?,
    )
}

@Composable
fun DailyChecklistContent(modifier: Modifier = Modifier, component: DailyChecklistComponent) {

    val viewModel by component.viewModel.collectAsState()

    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        canScroll = { scrollState.canScrollBackward || scrollState.canScrollForward }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(
                    state = scrollState,
                ),
        ) {
            viewModel.items.forEachIndexed { i, item ->
                DailyChecklistItem(
                    modifier = Modifier
                        .combinedClickable(
                            onClickLabel = stringResource(
                                if (item.checkedTime == null) {
                                    Res.string.daily_checklist_item_check
                                } else {
                                    Res.string.daily_checklist_item_uncheck
                                }
                            ),
                            onLongClickLabel = "TODO",
                            onClick = { component.onItemClick(item.id) },
                            onLongClick = { component.onItemLongClick(item.id) }
                        )
                        .minimumInteractiveComponentSize(),
                    viewModel = item,
                    listIndex = i,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun DailyChecklistItem(
    modifier: Modifier = Modifier,
    viewModel: DailyChecklistViewModel.Item,
    listIndex: Int,
) {
    ListItem(modifier = modifier) {
        Row {
            DailyChecklistItemName(
                modifier = Modifier.weight(1f),
                viewModel = viewModel,
                listIndex = listIndex,
            )
            if (viewModel.checkedTime == null) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    painter = painterResource(Res.drawable.pending),
                    contentDescription = stringResource(
                        Res.string.daily_checklist_item_pending
                    ),
                )
            } else {
                HourMinuteText(
                    modifier = Modifier.padding(start = 8.dp),
                    instant = viewModel.checkedTime,
                )
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    painter = painterResource(Res.drawable.check_circle),
                    contentDescription = stringResource(
                        Res.string.daily_checklist_item_complete
                    ),
                )
            }
        }
    }
}

@Composable
private fun DailyChecklistItemName(
    modifier: Modifier = Modifier,
    viewModel: DailyChecklistViewModel.Item,
    listIndex: Int,
) {
    if (viewModel.name == null) {
        Text(
            modifier = modifier,
            maxLines = 1,
            text = stringResource(Res.string.daily_checklist_item_ordinal, listIndex + 1),
            style = MaterialTheme.typography.unfilledBody,
        )
    } else {
        Text(
            modifier = modifier,
            maxLines = 1,
            text = viewModel.name,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
