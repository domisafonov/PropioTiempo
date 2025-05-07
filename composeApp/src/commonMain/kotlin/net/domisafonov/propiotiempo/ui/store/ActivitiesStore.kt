package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.model.PtSettings
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.Intent
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.Label
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.State
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.usecase.GetSettingsUc
import net.domisafonov.propiotiempo.data.usecase.SetSettingUc

interface ActivitiesStore : Store<Intent, State, Label> {

    sealed interface Intent {
        object ToggleDailyChecklists : Intent
        object ToggleTimedActivities : Intent
        data class ClickTimedActivity(val id: Long) : Intent
        data class ClickDailyChecklist(val id: Long) : Intent
    }

    @Serializable
    data class State(
        val dailyChecklists: List<ActivityRepository.ChecklistSummary>,
        val timedActivities: List<ActivityRepository.TimeActivitySummary>,
        val isDailyChecklistViewActive: Boolean, // TODO: persist
        val isTimedActivitiesViewActive: Boolean, // TODO: persist
    )

    sealed interface Label {
        data class NavigateToDailyChecklist(val id: Long) : Label
    }

    companion object
}

private sealed interface Action {
    data object ReadSettings : Action
    data object SubToActivities : Action
}

private sealed interface Message {

    data class SetSettings(
        val isDailyChecklistViewActive: Boolean,
        val isTimedActivitiesViewActive: Boolean,
    ) : Message

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
    getSettingsUc: GetSettingsUc,
    setSettingsUc: SetSettingUc,
): ActivitiesStore = object : ActivitiesStore, Store<Intent, State, Label> by create(
    name = ActivitiesStore::class.simpleName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.simpleName!!,
            strategy = State.serializer(),
        )
        ?: ActivitiesStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {
        dispatch(Action.ReadSettings)
        dispatch(Action.SubToActivities)
    },
    executorFactory = coroutineExecutorFactory {

        onAction<Action.ReadSettings> {
            launch {
                val settings = getSettingsUc.execute()
                dispatch(
                    Message.SetSettings(
                        isDailyChecklistViewActive = settings.isActivityTabDailyChecklistsSectionOpen,
                        isTimedActivitiesViewActive = settings.isActivityTabTimedActivitiesSectionOpen,
                    )
                )
            }
        }

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
            val new = !state().isDailyChecklistViewActive
            launch {
                setSettingsUc.execute(
                    property = PtSettings::isActivityTabDailyChecklistsSectionOpen,
                    value = new,
                )
            }
            dispatch(
                message = Message.ActivateDailyChecklists(
                    isActive = new,
                ),
            )
        }

        onIntent<Intent.ToggleTimedActivities> {
            val new = !state().isTimedActivitiesViewActive
            launch {
                setSettingsUc.execute(
                    property = PtSettings::isActivityTabTimedActivitiesSectionOpen,
                    value = new,
                )
            }
            dispatch(
                message = Message.ActivateTimedActivities(
                    isActive = new,
                ),
            )
        }

        onIntent<Intent.ClickTimedActivity> {
            launch {
                activityRepository.toggleTimedActivity(id = it.id)
            }
        }

        onIntent<Intent.ClickDailyChecklist> {
            publish(Label.NavigateToDailyChecklist(it.id))
        }
    },
    reducer = { message: Message -> when (message) {
        is Message.SetSettings -> copy(
            isDailyChecklistViewActive = message.isDailyChecklistViewActive,
            isTimedActivitiesViewActive = message.isTimedActivitiesViewActive,
        )
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
