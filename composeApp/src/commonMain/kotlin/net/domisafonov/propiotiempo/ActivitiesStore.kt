package net.domisafonov.propiotiempo

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.ActivitiesStore.Intent
import net.domisafonov.propiotiempo.ActivitiesStore.Label
import net.domisafonov.propiotiempo.ActivitiesStore.State
import net.domisafonov.propiotiempo.data.ActivityRepository

interface ActivitiesStore : Store<Intent, State, Label> {

    sealed interface Intent

    @Serializable
    data class State(
        val dailyChecklists: List<ActivityRepository.ChecklistSummary>,
        val timedActivities: List<ActivityRepository.TimeActivitySummary>,
        val isDailyChecklistViewActive: Boolean,
        val isTimedActivitiesViewActive: Boolean,
    )

    sealed interface Label
}

private sealed interface Action
private sealed interface Message {

    data class ChecklistsUpdate(
        val checklists: List<ActivityRepository.ChecklistSummary>,
    ) : Message

    data class TimedActivitiesUpdate(
        val timedActivities: List<ActivityRepository.TimeActivitySummary>,
    ) : Message
}

private val INITIAL_STATE = State(
    dailyChecklists = emptyList(),
    timedActivities = emptyList(),
    isDailyChecklistViewActive = false,
    isTimedActivitiesViewActive = false,
)

fun StoreFactory.makeActivitiesStore(
    stateKeeper: StateKeeper?,
    activityRepository: ActivityRepository,
): ActivitiesStore =
    object : ActivitiesStore, Store<Intent, State, Label> by create(
        name = ActivitiesStore::class.simpleName,
        initialState = stateKeeper
            ?.consume(State::class.simpleName!!, State.serializer())
            ?: INITIAL_STATE,
        bootstrapper = coroutineBootstrapper {
            launch {
                activityRepository.observeTodaysChecklistSummary()
                    .collect { dispatch(Message.ChecklistsUpdate(checklists = it)) }
            }
            launch {
                activityRepository.observeTodaysTimeActivitySummary()
                    .collect {
                        dispatch(Message.TimedActivitiesUpdate(timedActivities = it))
                    }
            }
        },
        executorFactory = coroutineExecutorFactory {

        },
        reducer = { message: Message -> when (message) {
            is Message.ChecklistsUpdate -> copy(dailyChecklists = dailyChecklists)
            is Message.TimedActivitiesUpdate -> copy(timedActivities = timedActivities)
        } },
    ) {}.also { store ->
        stateKeeper?.register(
            key = State::class.simpleName!!,
            strategy = State.serializer(),
        ) { INITIAL_STATE }
    }
