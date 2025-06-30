package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.data.atDateOf
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Instant

fun interface UpdateDailyChecklistCheckTimeUc {
    suspend fun execute(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: LocalTime,
    ): PtError?
}

class UpdateDailyChecklistCheckTimeUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : UpdateDailyChecklistCheckTimeUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: LocalTime,
    ): PtError? =
        activityRepository.updateDailyChecklistCheckTime(
            dailyChecklistItemId = dailyChecklistItemId,
            oldTime = oldTime,
            newTime = newTime.atDateOf(oldTime),
        )
}
