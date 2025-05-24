package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Intent
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Label
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.State
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
        val x: Int,
    )

    sealed interface Label

    companion object
}

private object TimedActivityIntervalsStoreInternal {

    sealed interface Action

    sealed interface Message
}

val TimedActivityIntervalsStore.Companion.INITIAL_STATE get() = State(
    x = 1,
)

fun StoreFactory.makeTimedActivityIntervalsStore(
    stateKeeper: StateKeeper?,
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

    },
    executorFactory = coroutineExecutorFactory {

    },
    reducer = { message: Message -> when (message) {
        else -> copy()
    } },
) {}.also {
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { TimedActivityIntervalsStore.INITIAL_STATE }
}
