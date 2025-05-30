package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.error.ModificationError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface CheckDailyChecklistItemUc {
    suspend fun execute(dailyChecklistItemId: Long): ModificationError?
}

class CheckDailyChecklistItemUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : CheckDailyChecklistItemUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(dailyChecklistItemId: Long): ModificationError? =
        activityRepository.insertDailyChecklistCheck(
            dailyChecklistItemId = dailyChecklistItemId,
            time = clock.now(),
        )
}
