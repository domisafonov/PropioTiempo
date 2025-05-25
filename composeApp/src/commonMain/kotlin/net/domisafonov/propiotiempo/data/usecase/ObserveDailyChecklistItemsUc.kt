package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.DailyChecklistItem
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.resetPeriodically

fun interface ObserveDailyChecklistItemsUc {
    fun execute(dailyChecklistId: Long): Flow<List<DailyChecklistItem>>
}

class ObserveDailyChecklistItemsUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ObserveDailyChecklistItemsUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(dailyChecklistId: Long): Flow<List<DailyChecklistItem>> =
        resetPeriodically {
            activityRepository
                .observeDailyChecklistItems(
                    dailyChecklistId = dailyChecklistId,
                    dayStart = getDayStart(),
                )
        }
}
