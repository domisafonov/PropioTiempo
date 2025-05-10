package net.domisafonov.propiotiempo.ui.store.dialog

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.Label
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.State
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStoreInternal.Message

interface EditTimeDialogStore : Store<Intent, State, Label> {

    sealed interface Intent

    @Serializable
    data class State(
        val title: String,
        val time: LocalTime,
    )

    sealed interface Label

    companion object
}

private object EditTimeDialogStoreInternal {

    sealed interface Action

    sealed interface Message
}

fun EditTimeDialogStore.Companion.initialState(
    title: String,
    time: LocalTime,
) = State(
    title = title,
    time = time,
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

    },
    reducer = { message: Message -> when (message) {
        else -> TODO()
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { initialState }
}
