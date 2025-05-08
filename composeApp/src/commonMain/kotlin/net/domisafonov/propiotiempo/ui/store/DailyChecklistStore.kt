package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Intent
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Label
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.State
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStoreInternal.Message

interface DailyChecklistStore : Store<Intent, State, Label> {

    sealed interface Intent

    @Serializable
    data class State(
        val x: Int,
    )

    sealed interface Label

    companion object
}

private object DailyChecklistStoreInternal {

    sealed interface Action

    sealed interface Message
}

val DailyChecklistStore.Companion.INITIAL_STATE get() = State(
    x = 1,
)

fun StoreFactory.makeDailyChecklistStore(
    stateKeeper: StateKeeper?,

): DailyChecklistStore = object : DailyChecklistStore, Store<Intent, State, Label> by create(
    name = DailyChecklistStore::class.qualifiedName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.qualifiedName!!,
            strategy = State.serializer(),
        )
        ?: DailyChecklistStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {

    },
    executorFactory = coroutineExecutorFactory {

    },
    reducer = { message: Message -> when (message) {
        else -> TODO()
    } },
) {}.also { store ->
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { DailyChecklistStore.INITIAL_STATE }
}
