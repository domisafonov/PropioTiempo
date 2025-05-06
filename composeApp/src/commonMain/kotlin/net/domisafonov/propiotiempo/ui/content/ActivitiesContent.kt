@file:OptIn(ExperimentalFoundationApi::class)

package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.domisafonov.propiotiempo.component.ActivitiesComponent
import net.domisafonov.propiotiempo.data.formatDurationHoursMinutes
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.check_circle
import propiotiempo.composeapp.generated.resources.daily_checklist_complete
import propiotiempo.composeapp.generated.resources.daily_checklist_pending
import propiotiempo.composeapp.generated.resources.daily_checklists_header
import propiotiempo.composeapp.generated.resources.foldable_fold
import propiotiempo.composeapp.generated.resources.foldable_unfold
import propiotiempo.composeapp.generated.resources.keyboard_arrow_down
import propiotiempo.composeapp.generated.resources.keyboard_arrow_up
import propiotiempo.composeapp.generated.resources.pending
import propiotiempo.composeapp.generated.resources.timed_activities_header
import kotlin.time.Duration.Companion.seconds

data class ActivitiesViewModel(
    val dailyChecklists: List<Checklist>,
    val timeActivities: List<TimeActivity>,
    val areDailiesShown: Boolean,
    val areTimeActivitiesShown: Boolean,
) {
    data class Checklist(
        val id: Long,
        val name: String,
        val isCompleted: Boolean
    )

    data class TimeActivity(
        val id: Long,
        val name: String,
        val todaysSeconds: Long,
    )
}

@Composable
fun ActivitiesContent(modifier: Modifier = Modifier, component: ActivitiesComponent) {

    val viewModel by component.viewModel.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colors.surface)
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
                TimeActivityItem(viewModel = item)
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
                ChecklistItem(viewModel = item)
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
fun FoldableListHeader(
    modifier: Modifier = Modifier,
    isOpen: Boolean = false,
    text: String,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        elevation = 2.dp,
    ) { Row(modifier = Modifier.padding(PaddingValues(vertical = 12.dp))) {
        Text(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Start))
                .weight(1f),
            text = text,
            fontWeight = FontWeight.Bold,
        )

        val (icon, desc) = if (isOpen) {
            Res.drawable.keyboard_arrow_up to Res.string.foldable_fold
        } else {
            Res.drawable.keyboard_arrow_down to Res.string.foldable_unfold
        }
        Icon(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.End))
                .padding(PaddingValues(start = 12.dp)),
            painter = painterResource(icon),
            contentDescription = stringResource(desc),
        )
    } }
}

@Composable
fun TimeActivityItem(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel.TimeActivity,
) {
    ListItem(modifier = modifier) { Row {
        Text(
            modifier = Modifier.weight(1f),
            maxLines = 1,
            text = viewModel.name,
        )
        Text(
            modifier = Modifier,
            maxLines = 1,
            text = formatDurationHoursMinutes(viewModel.todaysSeconds.toInt()),
            fontFamily = FontFamily.Monospace,
        )
    } }
}

@Composable
fun ChecklistItem(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel.Checklist
) {
    ListItem(modifier = modifier) { Row {
        Text(
            modifier = Modifier.weight(1f),
            maxLines = 1,
            text = viewModel.name,
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
    } }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.safeContent.only(WindowInsetsSides.Horizontal)
            )
            .padding(vertical = 8.dp),
    ) {
        content()
    }
}
