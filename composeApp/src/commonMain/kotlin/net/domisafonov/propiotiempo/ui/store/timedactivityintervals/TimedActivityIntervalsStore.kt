@file:UseSerializers(InstantSerializer::class)

package net.domisafonov.propiotiempo.ui.store.timedactivityintervals

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.model.InstantSerializer
import net.domisafonov.propiotiempo.data.model.TimedActivityInterval
import net.domisafonov.propiotiempo.data.periodicTimer
import net.domisafonov.propiotiempo.data.toLocalTime
import net.domisafonov.propiotiempo.data.usecase.DeleteTimedActivityIntervalUc
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUc
import net.domisafonov.propiotiempo.data.usecase.ObserveDaysTimedActivityIntervalsUc
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalStartUc
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalTimeUc
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.TimedActivityIntervalsStore.Intent
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.TimedActivityIntervalsStore.Label
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.TimedActivityIntervalsStore.State
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.TimedActivityIntervalsStoreInternal.Action
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.TimedActivityIntervalsStoreInternal.Message
import kotlin.time.Clock
import kotlin.time.Instant

interface TimedActivityIntervalsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class EditInterval(
            val start: Instant,
        ) : Intent
        data class IntervalStartEditConfirmed(
            val oldStart: Instant,
            val newStart: Instant,
        ) : Intent
        data class ShowIntervalMenu(
            val start: Instant,
        ) : Intent
        data class DeleteItem(
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

    sealed interface Label {
        data class EditIntervalStart(
            val timedActivityId: Long,
            val laterThanOrEqual: LocalTime?,
            val intervalStart: Instant,
        ) : Label
        data class EditInterval(
            val intervalStart: Instant,
        ) : Label
        data class ShowMenu(
            val timedActivityId: Long,
            val intervalStart: Instant,
        ) : Label
        data class Error(val inner: PtError) : Label
    }

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
    clock: Clock,
    observeActivityNameUc: ObserveActivityNameUc,
    observeDaysTimedActivityIntervalsUc: ObserveDaysTimedActivityIntervalsUc,
    updateTimedActivityIntervalStartUc: UpdateTimedActivityIntervalStartUc,
    updateTimedActivityIntervalTimeUc: UpdateTimedActivityIntervalTimeUc,
    deleteTimedActivityIntervalUc: DeleteTimedActivityIntervalUc,
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
                    .collect {
                        dispatch(Message.UpdateTime(time = clock.now()))
                        dispatch(Message.UpdateIntervals(intervals = it))
                    }
            }
        }
        onAction<Action.SubToTime> {
            launch {
                periodicTimer(clock = clock, doEmitMinutely = true)
                    .collect { dispatch(Message.UpdateTime(time = clock.now())) }
            }
        }
        onIntent<Intent.EditInterval> { intent ->
            val state = state()
            val interval = (state as? State.Ready)?.intervals
                ?.find { it.start == intent.start }
                ?: return@onIntent // TODO: null logging
            publish(
                if (interval.end == null) {
                    Label.EditIntervalStart(
                        timedActivityId = timedActivityId,
                        laterThanOrEqual = state.intervals
                            .getOrNull(state.intervals.size - 2)
                            ?.end
                            ?.toLocalTime(),
                        intervalStart = interval.start,
                    )
                } else {
                    Label.EditInterval(intervalStart = interval.start)
                }
            )
        }
        onIntent<Intent.IntervalStartEditConfirmed> { intent ->
            launch {
                updateTimedActivityIntervalStartUc
                    .execute(
                        activityId = timedActivityId,
                        oldStart = intent.oldStart,
                        newStart = intent.newStart,
                    )
                    ?.let { publish(Label.Error(it)) }
            }
        }
        onIntent<Intent.ShowIntervalMenu> { intent ->
            val interval = (state() as? State.Ready)?.intervals
                ?.find { it.start == intent.start }
                ?: return@onIntent // TODO: null logging
            publish(
                Label.ShowMenu(
                    timedActivityId = timedActivityId,
                    intervalStart = interval.start,
                )
            )
        }
        onIntent<Intent.DeleteItem> { intent ->
            launch {
                deleteTimedActivityIntervalUc
                    .execute(
                        activityId = timedActivityId,
                        start = intent.start,
                    )
                    ?.let { publish(Label.Error(it)) }
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
