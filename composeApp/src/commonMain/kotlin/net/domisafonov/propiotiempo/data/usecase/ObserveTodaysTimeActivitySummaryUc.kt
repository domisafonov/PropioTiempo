package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.TimeActivitySummary
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.resetPeriodically

interface ObserveTodaysTimeActivitySummaryUc {

    fun execute(): Flow<List<TimeActivitySummary>>
}

class ObserveTodaysTimeActivitySummaryUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ObserveTodaysTimeActivitySummaryUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(): Flow<List<TimeActivitySummary>> =
        resetPeriodically(doResetMinutely = true) {
            activityRepository.observeTodaysTimeActivitySummary(dayStart = getDayStart())
        }
}
