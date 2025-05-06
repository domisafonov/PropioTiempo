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

    sealed interface Intent {
        object ToggleDailyChecklists : Intent
        object ToggleTimedActivities : Intent
    }

    @Serializable
    data class State(
        val dailyChecklists: List<ActivityRepository.ChecklistSummary>,
        val timedActivities: List<ActivityRepository.TimeActivitySummary>,
        val isDailyChecklistViewActive: Boolean,
        val isTimedActivitiesViewActive: Boolean,
    )

    sealed interface Label

    companion object
}

private sealed interface Action {
    data object SubToActivities : Action
}

private sealed interface Message {

    data class ChecklistsUpdate(
        val dailyChecklists: List<ActivityRepository.ChecklistSummary>,
    ) : Message

    data class TimedActivitiesUpdate(
        val timedActivities: List<ActivityRepository.TimeActivitySummary>,
    ) : Message

    data class ActivateDailyChecklists(val isActive: Boolean) : Message
    data class ActivateTimedActivities(val isActive: Boolean) : Message
}

val ActivitiesStore.Companion.INITIAL_STATE get() = State(
    dailyChecklists = emptyList(),
    timedActivities = emptyList(),
    isDailyChecklistViewActive = true,
    isTimedActivitiesViewActive = true,
)

fun StoreFactory.makeActivitiesStore(
    stateKeeper: StateKeeper?,
    activityRepository: ActivityRepository,
): ActivitiesStore = object : ActivitiesStore, Store<Intent, State, Label> by create(
    name = ActivitiesStore::class.simpleName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.simpleName!!,
            strategy = State.serializer(),
        )
        ?: ActivitiesStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {
        dispatch(Action.SubToActivities)
    },
    executorFactory = coroutineExecutorFactory {
        onAction<Action.SubToActivities> {
            launch {
                activityRepository.observeTodaysChecklistSummary()
                    .collect {
                        dispatch(Message.ChecklistsUpdate(dailyChecklists = it))
                    }
            }
            launch {
                activityRepository.observeTodaysTimeActivitySummary()
                    .collect {
                        dispatch(Message.TimedActivitiesUpdate(timedActivities = it))
                    }
            }
        }
        onIntent<Intent.ToggleDailyChecklists> {
            dispatch(
                message = Message.ActivateDailyChecklists(
                    isActive = !state().isDailyChecklistViewActive,
                ),
            )
        }
        onIntent<Intent.ToggleTimedActivities> {
            dispatch(
                message = Message.ActivateTimedActivities(
                    isActive = !state().isTimedActivitiesViewActive,
                ),
            )
        }
    },
    reducer = { message: Message -> when (message) {
        is Message.ChecklistsUpdate -> copy(dailyChecklists = message.dailyChecklists)
        is Message.TimedActivitiesUpdate -> copy(timedActivities = message.timedActivities)
        is Message.ActivateDailyChecklists -> copy(isDailyChecklistViewActive = message.isActive)
        is Message.ActivateTimedActivities -> copy(isTimedActivitiesViewActive = message.isActive)
    } },
) {}.also { store ->
    stateKeeper?.register(
        key = State::class.simpleName!!,
        strategy = State.serializer(),
    ) { ActivitiesStore.INITIAL_STATE }
}
