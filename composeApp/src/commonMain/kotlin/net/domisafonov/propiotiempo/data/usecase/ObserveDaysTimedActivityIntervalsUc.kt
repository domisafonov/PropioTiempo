package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.TimedActivityInterval
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Clock

fun interface ObserveDaysTimedActivityIntervalsUc {
    fun execute(activityId: Long): Flow<List<TimedActivityInterval>>
}

class ObserveDaysTimedActivityIntervalsUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : ObserveDaysTimedActivityIntervalsUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(activityId: Long): Flow<List<TimedActivityInterval>> =
        activityRepository
            .observeDaysTimedActivityIntervals(
                activityId = activityId,
                dayStart = getDayStart(clock = clock),
            )
}
