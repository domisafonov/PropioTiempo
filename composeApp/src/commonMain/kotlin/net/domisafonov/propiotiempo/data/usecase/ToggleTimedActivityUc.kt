package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

interface ToggleTimedActivityUc {
    suspend fun execute(id: Long)
}

class ToggleTimedActivityUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ToggleTimedActivityUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(id: Long) {
        activityRepository.toggleTimedActivity(id = id, now = Clock.System.now())
    }
}
