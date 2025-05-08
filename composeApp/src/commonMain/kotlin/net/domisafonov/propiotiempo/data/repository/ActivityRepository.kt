package net.domisafonov.propiotiempo.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.model.ChecklistSummary
import net.domisafonov.propiotiempo.data.model.DailyChecklistItem
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

    suspend fun toggleTimedActivity(timedActivityId: Long, now: Instant)

    fun observeDailyChecklistName(id: Long): Flow<String>

    /**
     * Observe items of a daily checklist for the day starting at [dayStart]
     */
    fun observeDailyChecklistItems(
        dailyChecklistId: Long,
        dayStart: Instant,
    ): Flow<List<DailyChecklistItem>>
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
        timedActivityId: Long,
        now: Instant,
    ) = withContext(ioDispatcher) {
        dbQueries.transactionWithResult {
            val startTime = dbQueries
                .get_active_time_activity_interval(activity_id = timedActivityId)
                .executeAsOneOrNull()
            if (startTime == null) {
                dbQueries.insert_time_activity_interval(
                    activity_id = timedActivityId,
                    start_time = now,
                )
            } else {
                dbQueries.end_time_activity_interval(
                    activity_id = timedActivityId,
                    start_time = startTime,
                    end_time = now,
                )
            }
        }
    }

    override fun observeDailyChecklistName(id: Long): Flow<String> =
        dbQueries
            .get_activity_name(id = id)
            .asFlow()
            .mapToOne(ioDispatcher)

    override fun observeDailyChecklistItems(
        dailyChecklistId: Long,
        dayStart: Instant,
    ): Flow<List<DailyChecklistItem>> =
        dbQueries
            .get_daily_checklist_items(
                day_start = dayStart,
                daily_checklist_id = dailyChecklistId,
            ) { id, name, checked_time ->
                DailyChecklistItem(
                    id = id,
                    name = name,
                    checkedTime = checked_time,
                )
            }
            .asFlow()
            .mapToList(ioDispatcher)
}
