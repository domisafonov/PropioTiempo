package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface ToggleTimedActivityUc {
    suspend fun execute(id: Long): PtError?
}

class ToggleTimedActivityUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
    private val clock: Clock,
) : ToggleTimedActivityUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(id: Long): PtError? =
        activityRepository
            .toggleTimedActivity(
                timedActivityId = id,
                now = clock.now(),
            )
}
