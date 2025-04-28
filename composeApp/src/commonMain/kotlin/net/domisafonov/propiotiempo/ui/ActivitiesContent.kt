@file:OptIn(ExperimentalUuidApi::class)

package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.ActivitiesComponent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ActivitiesViewModel(
    val dailyChecklists: List<Checklist>,
    val timeActivities: List<TimeActivity>,
    val areDailiesShown: Boolean,
    val areTimeActivitiesShown: Boolean,
) {
    data class Checklist(
        val id: Long,
        val name: String,
        val items: List<Item>,
    ) {
        data class Item(
            val id: Uuid,
            val name: String?,
            val checkTime: Instant?,
        )
    }

    data class TimeActivity(
        val id: Long,
        val name: String,
        val previousPeriods: List<Pair<Instant, Instant>>,
        val currentPeriod: Pair<Instant, Instant?>?,
    )
}

@Composable
fun ActivitiesContent(modifier: Modifier = Modifier, component: ActivitiesComponent) {
    Text(
        modifier = modifier.windowInsetsPadding(WindowInsets.safeContent),
        text = "aaaaaaa",
    )
}
