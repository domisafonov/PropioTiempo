package net.domisafonov.propiotiempo.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.error.ModificationError
import net.domisafonov.propiotiempo.data.model.ChecklistSummary
import net.domisafonov.propiotiempo.data.model.DailyChecklistItem
import net.domisafonov.propiotiempo.data.model.TimedActivitySummary
import net.domisafonov.propiotiempo.data.model.TimedActivityInterval

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
    fun observeTodaysTimedActivitySummary(
        currentTime: Instant,
        dayStart: Instant,
    ): Flow<List<TimedActivitySummary>>

    suspend fun toggleTimedActivity(
        timedActivityId: Long,
        now: Instant,
    ): ModificationError?

    fun observeActivityName(id: Long): Flow<String>

    /**
     * Observe items of a daily checklist for the day starting at [dayStart]
     */
    fun observeDailyChecklistItems(
        dailyChecklistId: Long,
        dayStart: Instant,
    ): Flow<List<DailyChecklistItem>>

    suspend fun insertDailyChecklistCheck(
        dailyChecklistItemId: Long,
        time: Instant,
    ): ModificationError?

    suspend fun deleteDailyChecklistCheck(
        dailyChecklistItemId: Long,
        time: Instant,
    ): ModificationError?

    suspend fun updateDailyChecklistCheckTime(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: Instant,
    ): ModificationError?

    fun observeDaysTimedActivityIntervals(
        activityId: Long,
        dayStart: Instant,
    ): Flow<List<TimedActivityInterval>>

    suspend fun updateTimeActivityIntervalStart(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
    ): ModificationError?

    suspend fun updateTimeActivityIntervalTime(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
        newEnd: Instant,
    ): ModificationError?

    suspend fun deleteTimeActivityInterval(
        activityId: Long,
        start: Instant,
    ): ModificationError?
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

    override fun observeTodaysTimedActivitySummary(
        currentTime: Instant,
        dayStart: Instant,
    ): Flow<List<TimedActivitySummary>> =
        dbQueries
            .get_time_activities_summary(
                day_start = dayStart.epochSeconds,
                current_time = currentTime.epochSeconds,
            ) { id, name, sum, is_active ->
                TimedActivitySummary(
                    id = id,
                    name = name,
                    todaysSeconds = sum.toLong(),
                    isActive = is_active,
                )
            }
            .asFlow()
            .mapToList(ioDispatcher)

    override suspend fun toggleTimedActivity(
        timedActivityId: Long,
        now: Instant,
    ): ModificationError? = withContext(ioDispatcher) {
        try {
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
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override fun observeActivityName(id: Long): Flow<String> =
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

    override suspend fun insertDailyChecklistCheck(
        dailyChecklistItemId: Long,
        time: Instant,
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries.insert_daily_checklist_check(
                daily_checklist_item_id = dailyChecklistItemId,
                time = time,
            )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override suspend fun deleteDailyChecklistCheck(
        dailyChecklistItemId: Long,
        time: Instant,
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries.delete_daily_checklist_check(
                daily_checklist_item_id = dailyChecklistItemId,
                time = time,
            )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override suspend fun updateDailyChecklistCheckTime(
        dailyChecklistItemId: Long,
        oldTime: Instant,
        newTime: Instant,
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries.update_daily_checklist_check_time(
                new_time = newTime,
                daily_checklist_item_id = dailyChecklistItemId,
                old_time = oldTime,
            )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override fun observeDaysTimedActivityIntervals(
        activityId: Long,
        dayStart: Instant,
    ): Flow<List<TimedActivityInterval>> =
        dbQueries
            .get_days_time_activity_intervals(
                time_activity_id = activityId,
                day_start = dayStart,
            ) { start_time, end_time ->
                TimedActivityInterval(
                    activityId = activityId,
                    start = start_time,
                    end = end_time,
                )
            }
            .asFlow()
            .mapToList(ioDispatcher)

    override suspend fun updateTimeActivityIntervalStart(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries
                .update_time_activity_interval_start(
                    new_start_time = newStart,
                    activity_id = activityId,
                    old_start_time = oldStart,
                )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override suspend fun updateTimeActivityIntervalTime(
        activityId: Long,
        oldStart: Instant,
        newStart: Instant,
        newEnd: Instant
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries
                .update_time_activity_interval_time(
                    new_start_time = newStart,
                    new_end_time = newEnd,
                    activity_id = activityId,
                    old_start_time = oldStart,
                )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }

    override suspend fun deleteTimeActivityInterval(
        activityId: Long,
        start: Instant
    ): ModificationError? = withContext(ioDispatcher) {
        try {
            dbQueries
                .delete_time_activity_interval(
                    activity_id = activityId,
                    start_time = start,
                )
            null
        } catch (e: Exception) {
            ModificationError(cause = e)
        }
    }
}
