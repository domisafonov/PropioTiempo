@file:OptIn(ExperimentalUuidApi::class, ExperimentalFoundationApi::class)

package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.domisafonov.propiotiempo.component.ActivitiesComponent
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.foldable_fold
import propiotiempo.composeapp.generated.resources.foldable_unfold
import propiotiempo.composeapp.generated.resources.keyboard_arrow_down
import propiotiempo.composeapp.generated.resources.keyboard_arrow_up
import kotlin.uuid.ExperimentalUuidApi

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

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeContent)) }
        stickyHeader {
            TimeActivitiesHeader(
                viewModel = viewModel,
                onClick = component::onTimedActivitiesToggled,
            )
        }
        items(items = listOf<ActivitiesViewModel.TimeActivity>(), key = ActivitiesViewModel.TimeActivity::id) {
            TimeActivityItem()
            HorizontalDivider()
        }
        stickyHeader {
            ChecklistsHeader(
                viewModel = viewModel,
                onClick = component::onDailyChecklistToggled,
            )
        }
        items(items = listOf<ActivitiesViewModel.Checklist>(), key = ActivitiesViewModel.Checklist::id) {
            ChecklistItem()
            HorizontalDivider()
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
        text = "AAAaaaaaaa",
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
        text = "Bbbbbbbbbbb",
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
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .background(color = Color.Red)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(PaddingValues(vertical = 12.dp))
    ) {
        Text(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Start))
                .weight(1f),
            text = text,
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
    }
}

@Composable
fun TimeActivityItem(modifier: Modifier = Modifier) {

}

@Composable
fun ChecklistItem(modifier: Modifier = Modifier) {

}
