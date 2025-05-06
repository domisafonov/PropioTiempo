package net.domisafonov.propiotiempo.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.db.DatabaseSource

interface ActivityRepository {

    /**
     * Observe summary of daily checklists, sorted by name
     *
     * Each entry sums up an enabled checklist without historical data.
     */
    fun observeTodaysChecklistSummary(): Flow<List<ChecklistSummary>>

    @Serializable
    data class ChecklistSummary(
        val id: Long,
        val name: String,
        val isCompleted: Boolean,
    )

    /**
     * Observe summary of time activities, sorted by name
     *
     * Each entry sums up an enabled time activity without historical data.
     */
    fun observeTodaysTimeActivitySummary(): Flow<List<TimeActivitySummary>>

    @Serializable
    data class TimeActivitySummary(
        val id: Long,
        val name: String,
        val todaysSeconds: Long,
        val isActive: Boolean,
    )

    suspend fun toggleTimedActivity(id: Long)
}

// TODO: extract time handling
class ActivityRepositoryImpl(
    database: DatabaseSource,
) : ActivityRepository {

    private val dbQueries = database.dbQueries

    override fun observeTodaysChecklistSummary(): Flow<List<ActivityRepository.ChecklistSummary>> =
        resetPeriodically {
            dbQueries
                .get_daily_checklist_summary(
                    day_start = getDayStart(),
                ) { id, name, is_completed ->
                    ActivityRepository.ChecklistSummary(
                        id = id,
                        name = name,
                        isCompleted = is_completed,
                    )
                }
                .asFlow()
        }.mapToList(Dispatchers.IO)

    override fun observeTodaysTimeActivitySummary(): Flow<List<ActivityRepository.TimeActivitySummary>> =
        resetPeriodically(doResetMinutely = true) {
            dbQueries
                .get_time_activities_summary(
                    day_start = getDayStart().epochSeconds,
                ) { id, name, sum, is_active ->
                    ActivityRepository.TimeActivitySummary(
                        id = id,
                        name = name,
                        todaysSeconds = sum.toLong(),
                        isActive = is_active,
                    )
                }
                .asFlow()
        }.mapToList(Dispatchers.IO)

    // TODO: error handling
    override suspend fun toggleTimedActivity(id: Long) = withContext(Dispatchers.IO) {
        dbQueries.transactionWithResult {
            val startTime = dbQueries
                .get_active_time_activity_interval(activity_id = id)
                .executeAsOneOrNull()
            if (startTime == null) {
                dbQueries.insert_time_activity_interval(
                    activity_id = id,
                    start_time = Clock.System.now(),
                )
            } else {
                dbQueries.end_time_activity_interval(
                    activity_id = id,
                    start_time = startTime,
                    end_time = Clock.System.now(),
                )
            }
        }
    }
}
