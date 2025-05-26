package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.model.TimedActivityInterval
import net.domisafonov.propiotiempo.data.periodicTimer
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
    sealed interface State {

        @Serializable
        data class Initializing(
            val activityName: String? = null,
            val intervals: List<TimedActivityInterval>? = null,
            val currentTime: Instant? = null,
        ) : State {

            fun tryInitialize(): State {
                return Ready(
                    activityName = activityName ?: return this,
                    intervals = intervals ?: return this,
                    currentTime = currentTime ?: return this,
                )
            }
        }

        @Serializable
        data class Ready(
            val activityName: String,
            val intervals: List<TimedActivityInterval>,
            val currentTime: Instant,
        ) : State
    }

    sealed interface Label

    companion object
}

private object TimedActivityIntervalsStoreInternal {

    sealed interface Action {
        data object SubToActivityName : Action
        data object SubToIntervals : Action
        data object SubToTime : Action
    }

    sealed interface Message {

        data class UpdateActivityName(
            val name: String,
        ) : Message

        data class UpdateIntervals(
            val intervals: List<TimedActivityInterval>,
        ) : Message

        data class UpdateTime(
            val time: Instant,
        ) : Message
    }
}

val TimedActivityIntervalsStore.Companion.INITIAL_STATE get() = State.Initializing()

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
        dispatch(Action.SubToTime)
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
        onAction<Action.SubToTime> {
            launch {
                periodicTimer(doEmitMinutely = true)
                    .collect { dispatch(Message.UpdateTime(time = Clock.System.now())) }
            }
        }
    },
    reducer = { message: Message -> when (this) {
        is State.Initializing -> when (message) {
            is Message.UpdateActivityName -> copy(activityName = message.name)
            is Message.UpdateIntervals -> copy(intervals = message.intervals)
            is Message.UpdateTime -> copy(currentTime = message.time)
        }.tryInitialize()

        is State.Ready -> when (message) {
            is Message.UpdateActivityName -> copy(activityName = message.name)
            is Message.UpdateIntervals -> copy(intervals = message.intervals)
            is Message.UpdateTime -> copy(currentTime = message.time)
        }
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { TimedActivityIntervalsStore.INITIAL_STATE }
}
