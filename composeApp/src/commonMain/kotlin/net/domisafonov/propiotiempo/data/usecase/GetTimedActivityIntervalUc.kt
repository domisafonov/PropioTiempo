package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.domisafonov.propiotiempo.data.model.LimitedLocalTimedActivityInterval
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Instant

fun interface GetTimedActivityIntervalUc {
    suspend fun execute(
        timedActivityId: Long,
        startTime: Instant,
    ): LimitedLocalTimedActivityInterval?
}

class GetTimedActivityIntervalUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : GetTimedActivityIntervalUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        timedActivityId: Long,
        startTime: Instant,
    ): LimitedLocalTimedActivityInterval? {
        val raw = activityRepository
            .getTimeActivityInterval(
                activityId = timedActivityId,
                start = startTime,
            )
            ?: return null

        val timeZone = TimeZone.currentSystemDefault()
        return LimitedLocalTimedActivityInterval(
            activityId = timedActivityId,
            timeZone = timeZone,
            start = raw.start.toLocalDateTime(timeZone = timeZone),
            end = raw.end?.toLocalDateTime(timeZone = timeZone),
            lowerEditLimit = raw.lowerEditLimit?.toLocalDateTime(timeZone = timeZone),
            upperEditLimit = raw.upperEditLimit?.toLocalDateTime(timeZone = timeZone),
        )
    }
}
