package net.domisafonov.propiotiempo.ui.component

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import kotlin.math.ceil

data class WheelPickerItem<I : Any>(
    val id: I,
    val name: String,
)

// TODO: edit on click (pass composable lambda in), accessibility
@Composable
fun<I : Any> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<WheelPickerItem<I>>,
    itemWidth: Dp = 86.dp,
    itemHeight: Dp = 48.dp,
    itemLines: Float = 2.5f,
    selected: I,
    onSelected: (id: I) -> Unit,
) {
    require(items.isNotEmpty())
    require(itemWidth.isFinite)
    require(itemHeight.isFinite)
    require(itemLines >= 1f)

    val height = itemHeight.value * itemLines
    val paddingHeight = (height - itemHeight.value) / 2.0f
    val paddingCount = ceil(paddingHeight / itemHeight.value).toInt()

    val indices = Indices(
        extras = paddingCount,
        internalSize = items.size,
    )

    val selectedPage = indices.toExternal(
        items.indexOfFirst { it.id == selected }
            .also { require(it != -1) }
    )

    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { indices.externalSize },
    )

    var oldItems by remember { mutableStateOf(items) }
    var oldSelection: I by remember { mutableStateOf(selected) }

    LaunchedEffect(items, pagerState.settledPage, pagerState.targetPage, pagerState.isScrollInProgress) {
        if (items != oldItems) {
            oldItems = items
            pagerState.requestScrollToPage(selectedPage)
            return@LaunchedEffect
        }

        val currentIdx = indices.toMaybeInternal(pagerState.settledPage)
        if (!pagerState.isScrollInProgress && pagerState.currentPageOffsetFraction != 0f) {
            pagerState.animateScrollToPage(
                if (indices.isAtStart(pagerState.settledPage, includeFirstInternal = true)) {
                    indices.internalRange.first
                } else {
                    indices.internalRange.last
                }
            )
            return@LaunchedEffect
        }
        currentIdx ?: return@LaunchedEffect

        if (pagerState.settledPage == pagerState.targetPage) {
            val currentId = items[currentIdx].id
            if (oldSelection != currentId) {
                onSelected(currentId)
            }
            oldSelection = currentId
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val transSurfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    val topFade = paddingHeight / height
    val bottomFade = (height - paddingHeight) / height
    VerticalPager(
        modifier = modifier
            .size(width = itemWidth, height = height.dp)
            .drawWithCache {
                val brush = Brush.verticalGradient(
                    0f to surfaceColor,
                    topFade to transSurfaceColor,
                    bottomFade to transSurfaceColor,
                    1f to surfaceColor,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = brush)
                }
            },
        state = pagerState,
        pageSize = PageSize.Fixed(itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        key = indices::idFromExternal,
        snapPosition = SnapPosition.Center,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(indices.externalSize),
        ),
    ) { i -> when {
        indices.isInner(i) -> Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = items[indices.toInternal(i)].name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        else -> Spacer(modifier = Modifier)
    } }
}
