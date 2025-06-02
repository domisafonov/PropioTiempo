package net.domisafonov.propiotiempo.ui.store.timedactivityintervals

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.usecase.DeleteTimedActivityIntervalUc
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalTimeUc
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.State
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.Label
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStoreInternal.Message

interface EditTimedActivityIntervalDialogStore : Store<Intent, State, Label> {

    sealed interface Intent

    @Serializable
    data class State(
        val x: Int,
    )

    sealed interface Label

    companion object
}

private object EditTimedActivityIntervalDialogStoreInternal {

    sealed interface Action

    sealed interface Message
}

val EditTimedActivityIntervalDialogStore.Companion.INITIAL_STATE get() = State(
    x = 5,
)

fun StoreFactory.makeEditTimedActivityIntervalDialogStore(
    stateKeeper: StateKeeper?,
    clock: Clock,
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

    },
    executorFactory = coroutineExecutorFactory {

    },
    reducer = { message: Message -> when (this) {
        else -> TODO()
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { EditTimedActivityIntervalDialogStore.INITIAL_STATE }
}
