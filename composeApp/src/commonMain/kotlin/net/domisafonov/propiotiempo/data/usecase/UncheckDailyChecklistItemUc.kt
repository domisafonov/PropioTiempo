package net.domisafonov.propiotiempo.data.usecase

import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Instant

fun interface UncheckDailyChecklistItemUc {
    suspend fun execute(dailyChecklistItemId: Long, time: Instant): PtError?
}

class UncheckDailyChecklistItemUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : UncheckDailyChecklistItemUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        dailyChecklistItemId: Long,
        time: Instant,
    ): PtError? =
        activityRepository.deleteDailyChecklistCheck(
            dailyChecklistItemId = dailyChecklistItemId,
            time = time,
        )
}
