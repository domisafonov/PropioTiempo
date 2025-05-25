package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.model.TimedActivityInterval
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUc
import net.domisafonov.propiotiempo.data.usecase.ObserveDaysTimedActivityIntervalsUc
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Intent
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Label
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.State
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStoreInternal.Action
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStoreInternal.Message

interface TimedActivityIntervalsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class EditInterval(
            val start: Instant,
        ) : Intent

        data class ShowIntervalMenu(
            val start: Instant,
        ) : Intent
    }

    @Serializable
    data class State(
        val activityName: String,
        val intervals: List<TimedActivityInterval>,
    )

    sealed interface Label

    companion object
}

private object TimedActivityIntervalsStoreInternal {

    sealed interface Action {
        data object SubToActivityName : Action
        data object SubToIntervals : Action
    }

    sealed interface Message {

        data class UpdateActivityName(
            val name: String,
        ) : Message

        data class UpdateIntervals(
            val intervals: List<TimedActivityInterval>,
        ) : Message
    }
}

val TimedActivityIntervalsStore.Companion.INITIAL_STATE get() = State(
    activityName = "",
    intervals = emptyList(),
)

fun StoreFactory.makeTimedActivityIntervalsStore(
    stateKeeper: StateKeeper?,
    observeActivityNameUc: ObserveActivityNameUc,
    observeDaysTimedActivityIntervalsUc: ObserveDaysTimedActivityIntervalsUc,
    timedActivityId: Long,
): TimedActivityIntervalsStore = object : TimedActivityIntervalsStore, Store<Intent, State, Label> by create(
    name = TimedActivityIntervalsStore::class.qualifiedName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.qualifiedName!!,
            strategy = State.serializer(),
        )
        ?: TimedActivityIntervalsStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {
        dispatch(Action.SubToActivityName)
        dispatch(Action.SubToIntervals)
    },
    executorFactory = coroutineExecutorFactory {
        onAction<Action.SubToActivityName> {
            launch {
                observeActivityNameUc.execute(id = timedActivityId)
                    .collect { dispatch(Message.UpdateActivityName(name = it)) }
            }
        }
        onAction<Action.SubToIntervals> {
            launch {
                observeDaysTimedActivityIntervalsUc.execute(timedActivityId)
                    .collect { dispatch(Message.UpdateIntervals(intervals = it)) }
            }
        }
    },
    reducer = { message: Message -> when (message) {
        is Message.UpdateActivityName -> copy(activityName = message.name)
        is Message.UpdateIntervals -> copy(intervals = message.intervals)
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { TimedActivityIntervalsStore.INITIAL_STATE }
}
