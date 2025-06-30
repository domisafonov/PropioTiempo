package net.domisafonov.propiotiempo.data.usecase

import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Clock

fun interface CheckDailyChecklistItemUc {
    suspend fun execute(dailyChecklistItemId: Long): PtError?
}

class CheckDailyChecklistItemUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : CheckDailyChecklistItemUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(dailyChecklistItemId: Long): PtError? =
        activityRepository.insertDailyChecklistCheck(
            dailyChecklistItemId = dailyChecklistItemId,
            time = clock.now(),
        )
}
