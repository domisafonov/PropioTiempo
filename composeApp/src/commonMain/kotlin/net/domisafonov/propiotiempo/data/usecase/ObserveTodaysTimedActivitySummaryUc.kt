package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.TimedActivitySummary
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.resetPeriodically

fun interface ObserveTodaysTimedActivitySummaryUc {

    fun execute(): Flow<List<TimedActivitySummary>>
}

class ObserveTodaysTimedActivitySummaryUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ObserveTodaysTimedActivitySummaryUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(): Flow<List<TimedActivitySummary>> =
        resetPeriodically(doResetMinutely = true) {
            activityRepository.observeTodaysTimedActivitySummary(dayStart = getDayStart())
        }
}
