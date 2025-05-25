package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface ObserveActivityNameUc {
    fun execute(id: Long): Flow<String>
}

class ObserveActivityNameUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
) : ObserveActivityNameUc {

    private val activityRepository by activityRepositoryProvider

    override fun execute(id: Long): Flow<String> =
        activityRepository.observeActivityName(id = id)
}
