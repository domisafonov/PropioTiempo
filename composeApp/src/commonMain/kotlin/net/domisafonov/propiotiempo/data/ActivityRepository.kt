package net.domisafonov.propiotiempo.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
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
    )
}

class ActivityRepositoryImpl(
    database: DatabaseSource,
) : ActivityRepository {

    private val dbQueries = database.dbQueries

    override fun observeTodaysChecklistSummary(): Flow<List<ActivityRepository.ChecklistSummary>> =
        resetAtMidnight {
            dbQueries
                .get_daily_checklist_summary(getDayStart()) { id, name, is_completed ->
                    ActivityRepository.ChecklistSummary(
                        id = id,
                        name = name,
                        isCompleted = is_completed,
                    )
                }
                .asFlow()
        }.mapToList(Dispatchers.IO)

    override fun observeTodaysTimeActivitySummary(): Flow<List<ActivityRepository.TimeActivitySummary>> =
        resetAtMidnight {
            dbQueries
                .get_time_activities_summary(getDayStart().epochSeconds) { id, name, sum ->
                    ActivityRepository.TimeActivitySummary(
                        id = id,
                        name = name,
                        todaysSeconds = sum.toLong(),
                    )
                }
                .asFlow()
        }.mapToList(Dispatchers.IO)
}
