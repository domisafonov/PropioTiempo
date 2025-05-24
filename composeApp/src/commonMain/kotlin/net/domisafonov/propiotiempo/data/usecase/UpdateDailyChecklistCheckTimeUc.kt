package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.data.atDateOf
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

interface UpdateDailyChecklistCheckTimeUc {
    suspend fun execute(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: LocalTime,
    )
}

class UpdateDailyChecklistCheckTimeUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : UpdateDailyChecklistCheckTimeUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: LocalTime,
    ) {
        activityRepository.updateDailyChecklistCheckTime(
            dailyChecklistItemId = dailyChecklistItemId,
            oldTime = oldTime,
            newTime = newTime.atDateOf(oldTime),
        )
    }
}
