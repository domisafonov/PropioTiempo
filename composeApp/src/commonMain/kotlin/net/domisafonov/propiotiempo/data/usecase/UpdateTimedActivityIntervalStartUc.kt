package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.error.TimeError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface UpdateTimedActivityIntervalStartUc {
    suspend fun execute(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
    ): PtError?
}

class UpdateTimedActivityIntervalStartUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
): UpdateTimedActivityIntervalStartUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
    ): PtError? {
        if (newStart > clock.now()) {
            return TimeError.LaterThanNow()
        }
        return activityRepository.updateTimeActivityIntervalStart(
            activityId = activityId,
            oldStart = oldStart,
            newStart = newStart,
        )
    }
}
