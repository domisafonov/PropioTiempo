package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.TimedActivitySummary
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.resetPeriodically
import kotlin.time.Clock

fun interface ObserveTodaysTimedActivitySummaryUc {

    fun execute(): Flow<List<TimedActivitySummary>>
}

class ObserveTodaysTimedActivitySummaryUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : ObserveTodaysTimedActivitySummaryUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(): Flow<List<TimedActivitySummary>> =
        resetPeriodically(clock = clock, doResetMinutely = true) {
            activityRepository.observeTodaysTimedActivitySummary(
                currentTime = clock.now(),
                dayStart = getDayStart(clock = clock),
            )
        }
}
