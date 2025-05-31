package net.domisafonov.propiotiempo.ui.store.dialog

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.withHour
import net.domisafonov.propiotiempo.data.withMinute
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.Label
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.State
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStoreInternal.Message

interface EditTimeDialogStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class UpdateHour(val hour: Int) : Intent
        data class UpdateMinute(val minute: Int) : Intent
    }

    @Serializable
    data class State(
        val title: String,
        val time: LocalTime,
        val timeRange: ClosedRange<LocalTime>,
    )

    sealed interface Label

    companion object
}

private object EditTimeDialogStoreInternal {

    sealed interface Action

    sealed interface Message {
        data class UpdateTime(val time: LocalTime) : Message
    }
}

fun EditTimeDialogStore.Companion.initialState(
    title: String,
    time: LocalTime,
    timeRange: ClosedRange<LocalTime>,
) = State(
    title = title,
    time = time,
    timeRange = timeRange,
)

fun StoreFactory.makeEditTimeDialogStore(
    stateKeeper: StateKeeper?,
    initialState: State,
): EditTimeDialogStore = object : EditTimeDialogStore, Store<Intent, State, Label> by create(
    name = EditTimeDialogStore::class.qualifiedName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.qualifiedName!!,
            strategy = State.serializer(),
        )
        ?: initialState,
    executorFactory = coroutineExecutorFactory {
        onIntent<Intent.UpdateHour> { intent ->
            val state = state()
            dispatch(
                Message.UpdateTime(
                    time = state.time.withHour(intent.hour)
                        .coerceIn(state.timeRange),
                )
            )
        }
        onIntent<Intent.UpdateMinute> { intent ->
            val state = state()
            dispatch(
                Message.UpdateTime(
                    time = state.time.withMinute(intent.minute)
                        .coerceIn(state.timeRange),
                )
            )
        }
    },
    reducer = { message: Message -> when (message) {
        is Message.UpdateTime -> copy(time = message.time)
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { initialState }
}
