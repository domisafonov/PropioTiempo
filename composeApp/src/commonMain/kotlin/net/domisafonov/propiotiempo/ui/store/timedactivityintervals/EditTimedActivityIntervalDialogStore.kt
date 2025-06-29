package net.domisafonov.propiotiempo.ui.store.timedactivityintervals

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.periodicTimer
import net.domisafonov.propiotiempo.data.usecase.DeleteTimedActivityIntervalUc
import net.domisafonov.propiotiempo.data.usecase.GetTimedActivityIntervalUc
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalTimeUc
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.State
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.Label
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStoreInternal.Action
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStoreInternal.Message

interface EditTimedActivityIntervalDialogStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ConfirmEdit : Intent
        data object CancelEdit : Intent
        data object DeleteInterval : Intent
    }

    @Serializable
    sealed interface State {

        @Serializable
        data class Initializing(
            val startTime: LocalDateTime? = null,
            val endTime: LocalDateTime? = null,
            val timeZone: TimeZone? = null,
            val currentTime: Instant? = null,
            val lowerEditLimit: LocalDateTime? = null,
            val upperEditLimit: LocalDateTime? = null,
        ) : State {

            fun tryInitialize(): State {
                val timeZone = timeZone ?: return this
                return Ready(
                    startTime = startTime ?: return this,
                    endTime = endTime ?: return this,
                    currentTime = currentTime
                        ?.toLocalDateTime(timeZone = timeZone)
                        ?: return this,
                    timeZone = timeZone,
                    lowerEditLimit = lowerEditLimit,
                    upperEditLimit = upperEditLimit,
                )
            }
        }

        @Serializable
        data class Ready(
            val startTime: LocalDateTime,
            val endTime: LocalDateTime,
            val currentTime: LocalDateTime,
            val timeZone: TimeZone,
            val lowerEditLimit: LocalDateTime?,
            val upperEditLimit: LocalDateTime?,
        ) : State
    }

    sealed interface Label {
        data object IntervalNotFound : Label
        data object IntervalIsActive : Label
    }

    companion object
}

private object EditTimedActivityIntervalDialogStoreInternal {

    sealed interface Action {
        data object ReadInterval : Action
        data object SubscribeToTime : Action
    }

    sealed interface Message {

        data class IntervalRead(
            val start: LocalDateTime,
            val end: LocalDateTime,
            val timeZone: TimeZone,
            val lowerEditLimit: LocalDateTime?,
            val upperEditLimit: LocalDateTime?,
        ) : Message

        data class TimeRead(
            val currentTime: Instant,
        ) : Message
    }
}

val EditTimedActivityIntervalDialogStore.Companion.INITIAL_STATE
    get() = State.Initializing()

fun StoreFactory.makeEditTimedActivityIntervalDialogStore(
    stateKeeper: StateKeeper?,
    clock: Clock,
    getTimedActivityIntervalUc: GetTimedActivityIntervalUc,
    updateTimedActivityIntervalTimeUc: UpdateTimedActivityIntervalTimeUc,
    deleteTimedActivityIntervalUc: DeleteTimedActivityIntervalUc,
    timedActivityId: Long,
    editedIntervalStart: Instant,
): EditTimedActivityIntervalDialogStore = object : EditTimedActivityIntervalDialogStore, Store<Intent, State, Label> by create(
    name = EditTimedActivityIntervalDialogStore::class.qualifiedName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.qualifiedName!!,
            strategy = State.serializer(),
        )
        ?: EditTimedActivityIntervalDialogStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {
        dispatch(Action.ReadInterval)
        dispatch(Action.SubscribeToTime)
    },
    executorFactory = coroutineExecutorFactory {
        onAction<Action.ReadInterval> {
            launch {
                val interval = getTimedActivityIntervalUc.execute(
                    timedActivityId = timedActivityId,
                    startTime = editedIntervalStart,
                ) ?: let {
                    publish(Label.IntervalNotFound)
                    return@launch
                }

                if (interval.end == null) {
                    publish(Label.IntervalIsActive)
                    return@launch
                }

                dispatch(
                    Message.IntervalRead(
                        start = interval.start,
                        end = interval.end,
                        timeZone = interval.timeZone,
                        lowerEditLimit = interval.lowerEditLimit,
                        upperEditLimit = interval.upperEditLimit,
                    )
                )
            }
        }
        onAction<Action.SubscribeToTime> {
            launch {
                periodicTimer(
                    clock = clock,
                    doEmitMinutely = true,
                ).collect {
                    dispatch(
                        Message.TimeRead(currentTime = it)
                    )
                }
            }
        }
        onIntent<Intent.ConfirmEdit> {
            TODO()
        }
        onIntent<Intent.CancelEdit> {
            TODO()
        }
        onIntent<Intent.DeleteInterval> {
            TODO()
        }
    },
    reducer = { message: Message -> when (this) {
        is State.Initializing -> when (message) {
            is Message.IntervalRead -> copy(
                startTime = message.start,
                endTime = message.end,
                timeZone = message.timeZone,
                lowerEditLimit = message.lowerEditLimit,
                upperEditLimit = message.upperEditLimit,
            )
            is Message.TimeRead -> copy(currentTime = message.currentTime)
        }.tryInitialize()
        is State.Ready -> when (message) {
            is Message.IntervalRead -> this // TODO: log
            is Message.TimeRead -> copy(
                currentTime = message.currentTime.toLocalDateTime(timeZone = timeZone),
            )
        }
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { EditTimedActivityIntervalDialogStore.INITIAL_STATE }
}
