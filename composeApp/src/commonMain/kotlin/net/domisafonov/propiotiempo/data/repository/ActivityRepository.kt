package net.domisafonov.propiotiempo.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.model.ChecklistSummary
import net.domisafonov.propiotiempo.data.model.TimeActivitySummary

interface ActivityRepository {

    /**
     * Observe summary of daily checklists, sorted by name
     *
     * Each entry sums up an enabled checklist without historical data.
     */
    fun observeTodaysChecklistSummary(
        dayStart: Instant,
    ): Flow<List<ChecklistSummary>>

    /**
     * Observe summary of time activities, sorted by name
     *
     * Each entry sums up an enabled time activity without historical data.
     */
    fun observeTodaysTimeActivitySummary(
        dayStart: Instant,
    ): Flow<List<TimeActivitySummary>>

    suspend fun toggleTimedActivity(id: Long, now: Instant)
}

class ActivityRepositoryImpl(
    database: DatabaseSource,
    private val ioDispatcher: CoroutineDispatcher,
) : ActivityRepository {

    private val dbQueries = database.dbQueries

    override fun observeTodaysChecklistSummary(
        dayStart: Instant,
    ): Flow<List<ChecklistSummary>> =
        dbQueries
            .get_daily_checklist_summary(
                day_start = dayStart,
            ) { id, name, is_completed ->
                ChecklistSummary(
                    id = id,
                    name = name,
                    isCompleted = is_completed,
                )
            }
            .asFlow()
            .mapToList(ioDispatcher)

    override fun observeTodaysTimeActivitySummary(
        dayStart: Instant,
    ): Flow<List<TimeActivitySummary>> =
        dbQueries
            .get_time_activities_summary(
                day_start = dayStart.epochSeconds,
            ) { id, name, sum, is_active ->
                TimeActivitySummary(
                    id = id,
                    name = name,
                    todaysSeconds = sum.toLong(),
                    isActive = is_active,
                )
            }
            .asFlow()
            .mapToList(ioDispatcher)

    // TODO: error handling
    override suspend fun toggleTimedActivity(
        id: Long,
        now: Instant,
    ) = withContext(ioDispatcher) {
        dbQueries.transactionWithResult {
            val startTime = dbQueries
                .get_active_time_activity_interval(activity_id = id)
                .executeAsOneOrNull()
            if (startTime == null) {
                dbQueries.insert_time_activity_interval(
                    activity_id = id,
                    start_time = now,
                )
            } else {
                dbQueries.end_time_activity_interval(
                    activity_id = id,
                    start_time = startTime,
                    end_time = now,
                )
            }
        }
    }
}
