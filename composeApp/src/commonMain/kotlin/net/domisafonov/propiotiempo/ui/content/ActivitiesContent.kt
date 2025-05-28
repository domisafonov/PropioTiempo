@file:OptIn(ExperimentalFoundationApi::class)

package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.domisafonov.propiotiempo.component.ActivitiesComponent
import net.domisafonov.propiotiempo.ui.component.FoldableListHeader
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import net.domisafonov.propiotiempo.ui.component.HourMinuteText
import net.domisafonov.propiotiempo.ui.component.ListItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.check_circle
import propiotiempo.composeapp.generated.resources.daily_checklist_complete
import propiotiempo.composeapp.generated.resources.daily_checklist_pending
import propiotiempo.composeapp.generated.resources.daily_checklists_header
import propiotiempo.composeapp.generated.resources.open_daily_checklist
import propiotiempo.composeapp.generated.resources.pending
import propiotiempo.composeapp.generated.resources.start_timed_activity
import propiotiempo.composeapp.generated.resources.stop_timed_activity
import propiotiempo.composeapp.generated.resources.timed_activities_header
import propiotiempo.composeapp.generated.resources.timed_activity_in_progress

@Immutable
data class ActivitiesViewModel(
    val dailyChecklists: List<Checklist>,
    val timeActivities: List<TimeActivity>,
    val areDailiesShown: Boolean,
    val areTimeActivitiesShown: Boolean,
) {
    // TODO: add n/m label
    @Immutable
    data class Checklist(
        val id: Long,
        val name: String,
        val isCompleted: Boolean
    )

    @Immutable
    data class TimeActivity(
        val id: Long,
        val name: String,
        val todaysSeconds: Long,
        val isActive: Boolean,
    )
}

@Composable
fun ActivitiesContent(modifier: Modifier = Modifier, component: ActivitiesComponent) {

    val viewModel by component.viewModel.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item {
            Spacer(
                modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeContent)
                    .fillMaxWidth(),
            )
        }
        stickyHeader {
            TimeActivitiesHeader(
                viewModel = viewModel,
                onClick = component::onTimedActivitiesToggled,
            )
        }
        if (viewModel.areTimeActivitiesShown) {
            items(
                items = viewModel.timeActivities,
                key = ActivitiesViewModel.TimeActivity::id,
            ) { item -> Column(modifier = Modifier.animateItem()) {
                TimeActivityItem(
                    viewModel = item,
                    onClick = { component.onTimedActivityClick(id = item.id) },
                    onLongClick = { component.onTimedActivityLongClick(id = item.id) }
                )
                HorizontalDivider()
            } }
        }
        stickyHeader {
            ChecklistsHeader(
                viewModel = viewModel,
                onClick = component::onDailyChecklistToggled,
            )
        }
        if (viewModel.areDailiesShown) {
            items(
                items = viewModel.dailyChecklists,
                key = ActivitiesViewModel.Checklist::id,
            ) { item -> Column(modifier = Modifier.animateItem()) {
                ChecklistItem(
                    viewModel = item,
                    onClick = { component.onDailyChecklistClick(id = item.id) },
                )
                HorizontalDivider()
            } }
        }
    }
}

@Composable
fun TimeActivitiesHeader(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel,
    onClick: () -> Unit = {},
) {
    FoldableListHeader(
        modifier = modifier,
        isOpen = viewModel.areTimeActivitiesShown,
        text = stringResource(Res.string.timed_activities_header),
        onClick = onClick,
    )
}

@Composable
fun ChecklistsHeader(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel,
    onClick: () -> Unit = {},
) {
    FoldableListHeader(
        modifier = modifier,
        isOpen = viewModel.areDailiesShown,
        text = stringResource(Res.string.daily_checklists_header),
        onClick = onClick,
    )
}

@Composable
fun TimeActivityItem(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel.TimeActivity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ListItem(
        modifier = modifier
            .combinedClickable(
                onClickLabel = stringResource(
                    if (viewModel.isActive) {
                        Res.string.start_timed_activity
                    } else {
                        Res.string.stop_timed_activity
                    }
                ),
                onLongClickLabel = "TODO",
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .minimumInteractiveComponentSize(),
        minSurfaceContentsSize = 24.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .weight(1f),
                maxLines = 1,
                text = viewModel.name,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (viewModel.isActive) {
                Icon(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    painter = painterResource(Res.drawable.pending),
                    contentDescription = stringResource(
                        Res.string.timed_activity_in_progress
                    ),
                )
            }

            HourMinuteText(seconds = viewModel.todaysSeconds.toInt())
        }
    }
}

@Composable
fun ChecklistItem(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel.Checklist,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier
            .clickable(
                onClickLabel = stringResource(Res.string.open_daily_checklist),
                onClick = onClick,
            )
            .minimumInteractiveComponentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                maxLines = 1,
                text = viewModel.name,
                style = MaterialTheme.typography.bodyMedium,
            )

            val (icon, description) = if (viewModel.isCompleted) {
                Res.drawable.check_circle to Res.string.daily_checklist_complete
            } else {
                Res.drawable.pending to Res.string.daily_checklist_pending
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(description),
            )
        }
    }
}
