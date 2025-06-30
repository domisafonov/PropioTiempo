package net.domisafonov.propiotiempo.data.usecase

import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import kotlin.time.Instant

fun interface DeleteTimedActivityIntervalUc {
    suspend fun execute(
        activityId: Long,
        start: Instant,
    ): PtError?
}

class DeleteTimedActivityIntervalUcImpl(
    activityRepositoryProvider: Lazy<ActivityRepository>,
): DeleteTimedActivityIntervalUc {

    private val activityRepository by activityRepositoryProvider

    override suspend fun execute(
        activityId: Long,
        start: Instant,
    ): PtError? =
        activityRepository.deleteTimeActivityInterval(
            activityId = activityId,
            start = start,
        )
}
