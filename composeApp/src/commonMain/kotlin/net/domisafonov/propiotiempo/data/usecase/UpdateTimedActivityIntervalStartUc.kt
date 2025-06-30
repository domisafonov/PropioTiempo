package net.domisafonov.propiotiempo.data.usecase

import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.error.TimeError
import net.domisafonov.propiotiempo.data.isSameMinute
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.toLocalTime
import kotlin.time.Clock
import kotlin.time.Instant

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
        val now = clock.now()
        val correctedNewStart = if (newStart > clock.now()) {
            val localNow = now.toLocalTime()
            if (newStart.toLocalTime().isSameMinute(localNow)) {
                now
            } else {
                return TimeError.LaterThanNow()
            }
        } else {
            newStart
        }
        return activityRepository.updateTimeActivityIntervalStart(
            activityId = activityId,
            oldStart = oldStart,
            newStart = correctedNewStart,
        )
    }
}
