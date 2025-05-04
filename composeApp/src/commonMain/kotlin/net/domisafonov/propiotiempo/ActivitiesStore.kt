package net.domisafonov.propiotiempo

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.ActivitiesStore.Intent
import net.domisafonov.propiotiempo.ActivitiesStore.Label
import net.domisafonov.propiotiempo.ActivitiesStore.State
import net.domisafonov.propiotiempo.data.ActivityRepository

interface ActivitiesStore : Store<Intent, State, Label> {
    sealed interface Intent
    @Serializable
    data class State(
        val x: Int
    )
    sealed interface Label
}

private sealed interface Action
private sealed interface Message

private val INITIAL_STATE = State(1)

fun StoreFactory.makeActivitiesStore(
    stateKeeper: StateKeeper?,
    activityRepository: ActivityRepository,
): ActivitiesStore =
    object : ActivitiesStore, Store<Intent, State, Label> by create(
        name = ActivitiesStore::class.simpleName,
        initialState = stateKeeper
            ?.consume(State::class.simpleName!!, State.serializer())
            ?: INITIAL_STATE,
        bootstrapper = coroutineBootstrapper {

        },
        executorFactory = coroutineExecutorFactory {

        },
        reducer = { message ->
            TODO()
        },
    ) {}.also { store ->
        stateKeeper?.register(
            key = State::class.simpleName!!,
            strategy = State.serializer(),
        ) { INITIAL_STATE }
    }
