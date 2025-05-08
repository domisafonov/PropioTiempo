package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

interface ObserveDailyChecklistNameUc {
    fun execute(id: Long): Flow<String>
}

class ObserveDailyChecklistNameUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ObserveDailyChecklistNameUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(id: Long): Flow<String> =
        activityRepository.observeDailyChecklistName(id = id)
}
