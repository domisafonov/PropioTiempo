package net.domisafonov.propiotiempo.data.usecase

import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.error.ModificationError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository

fun interface DeleteTimedActivityIntervalUc {
    suspend fun execute(
        activityId: Long,
        start: Instant,
    ): ModificationError?
}

class DeleteTimedActivityIntervalUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
): DeleteTimedActivityIntervalUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        activityId: Long,
        start: Instant,
    ): ModificationError? =
        activityRepository.deleteTimeActivityInterval(
            activityId = activityId,
            start = start,
        )
}
