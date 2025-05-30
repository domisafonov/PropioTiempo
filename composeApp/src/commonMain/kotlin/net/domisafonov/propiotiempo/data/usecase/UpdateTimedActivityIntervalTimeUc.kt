package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.error.TimeError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface UpdateTimedActivityIntervalTimeUc {
    suspend fun execute(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
        newEnd: Instant,
    ): PtError?
}

class UpdateTimedActivityIntervalTimeUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
): UpdateTimedActivityIntervalTimeUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
        newEnd: Instant,
    ): PtError? {
        require(newEnd >= newStart)
        if (newStart > clock.now()) {
            return TimeError.LaterThanNow()
        }
        return activityRepository.updateTimeActivityIntervalTime(
            activityId = activityId,
            oldStart = oldStart,
            newStart = newStart,
            newEnd = newEnd,
        )
    }
}
