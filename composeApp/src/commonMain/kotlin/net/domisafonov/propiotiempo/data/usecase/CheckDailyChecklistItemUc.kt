package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

interface CheckDailyChecklistItemUc {
    suspend fun execute(dailyChecklistItemId: Long)
}

class CheckDailyChecklistItemUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : CheckDailyChecklistItemUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(dailyChecklistItemId: Long) {
        activityRepository.insertDailyChecklistCheck(
            dailyChecklistItemId = dailyChecklistItemId,
            time = Clock.System.now(),
        )
    }
}
