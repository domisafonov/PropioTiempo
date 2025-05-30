package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.getDayStart
import net.domisafonov.propiotiempo.data.model.ChecklistSummary
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.resetPeriodically

fun interface ObserveTodaysChecklistSummaryUc {

    fun execute(): Flow<List<ChecklistSummary>>
}

class ObserveTodaysChecklistSummaryUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : ObserveTodaysChecklistSummaryUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(): Flow<List<ChecklistSummary>> =
        resetPeriodically(clock = clock) {
            activityRepository.observeTodaysChecklistSummary(
                dayStart = getDayStart(clock = clock),
            )
        }
}
