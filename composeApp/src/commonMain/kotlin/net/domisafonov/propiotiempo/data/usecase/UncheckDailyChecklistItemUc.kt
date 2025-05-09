package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

interface UncheckDailyChecklistItemUc {
    suspend fun execute(dailyChecklistItemId: Long, time: Instant)
}

class UncheckDailyChecklistItemUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : UncheckDailyChecklistItemUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(dailyChecklistItemId: Long, time: Instant) {
        activityRepository.deleteDailyChecklistCheck(
            dailyChecklistItemId = dailyChecklistItemId,
            time = time,
        )
    }
}
