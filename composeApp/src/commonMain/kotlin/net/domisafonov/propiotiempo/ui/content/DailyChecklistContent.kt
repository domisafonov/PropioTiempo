package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.DailyChecklistComponent
import net.domisafonov.propiotiempo.ui.component.HorizontalDivider
import net.domisafonov.propiotiempo.ui.component.HourMinuteText
import net.domisafonov.propiotiempo.ui.component.ListItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.check_circle
import propiotiempo.composeapp.generated.resources.daily_checklist_item_complete
import propiotiempo.composeapp.generated.resources.daily_checklist_item_ordinal
import propiotiempo.composeapp.generated.resources.daily_checklist_item_pending
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .verticalScroll(
                state = rememberScrollState(),
            ),
    ) {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.safeDrawing)
        )

        Text(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Horizontal)
                        .union(WindowInsets(8.dp, 0.dp, 4.dp, 0.dp))
                )
                .padding(vertical = 12.dp),
            text = viewModel.name,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

        viewModel.items.forEachIndexed { i, item ->
            DailyChecklistItem(
                modifier = Modifier
                    .clickable { TODO() },
                viewModel = item,
                listIndex = i,
            )
            HorizontalDivider()
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
            fontWeight = FontWeight.Light,
        )
    } else {
        Text(
            modifier = modifier,
            maxLines = 1,
            text = viewModel.name,
        )
    }
}
