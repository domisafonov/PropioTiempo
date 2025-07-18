package net.domisafonov.propiotiempo.ui.store

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.error.PtError
import net.domisafonov.propiotiempo.data.model.DailyChecklistItem
import net.domisafonov.propiotiempo.data.usecase.CheckDailyChecklistItemUc
import net.domisafonov.propiotiempo.data.usecase.ObserveDailyChecklistItemsUc
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUc
import net.domisafonov.propiotiempo.data.usecase.UncheckDailyChecklistItemUc
import net.domisafonov.propiotiempo.data.usecase.UpdateDailyChecklistCheckTimeUc
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Intent
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Label
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.State
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStoreInternal.Action
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStoreInternal.Message
import kotlin.time.Instant

interface DailyChecklistStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ToggleItem(
            val id: Long,
        ) : Intent

        data class EditItem(
            val id: Long,
        ) : Intent

        data class ItemUncheckConfirmed(
            val id: Long,
        ) : Intent

        data class UpdateItemsTime(
            val itemId: Long,
            val oldTime: Instant,
            val newTime: LocalTime,
        ) : Intent
    }

    @Serializable
    data class State(
        val name: String,
        val items: List<DailyChecklistItem>,
    )

    sealed interface Label {
        data class ConfirmUncheckingItem(val id: Long): Label
        data class EditCheckingTime(val id: Long) : Label
        data class Error(val inner: PtError) : Label
    }

    companion object
}

private object DailyChecklistStoreInternal {

    sealed interface Action {
        data object SubToName : Action
        data object SubToItems : Action
    }

    sealed interface Message {

        data class NameUpdate(
            val name: String,
        ) : Message

        data class ItemsUpdate(
            val items: List<DailyChecklistItem>,
        ) : Message
    }
}

val DailyChecklistStore.Companion.INITIAL_STATE get() = State(
    name = "",
    items = emptyList(),
)

fun StoreFactory.makeDailyChecklistStore(
    stateKeeper: StateKeeper?,
    observeDailyChecklistItemsUc: ObserveDailyChecklistItemsUc,
    observeActivityNameUc: ObserveActivityNameUc,
    checkDailyChecklistItemUc: CheckDailyChecklistItemUc,
    uncheckDailyChecklistItemUc: UncheckDailyChecklistItemUc,
    updateDailyChecklistCheckTimeUc: UpdateDailyChecklistCheckTimeUc,
    dailyChecklistId: Long,
): DailyChecklistStore = object : DailyChecklistStore, Store<Intent, State, Label> by create(
    name = DailyChecklistStore::class.qualifiedName,
    initialState = stateKeeper
        ?.consume(
            key = State::class.qualifiedName!!,
            strategy = State.serializer(),
        )
        ?: DailyChecklistStore.INITIAL_STATE,
    bootstrapper = coroutineBootstrapper {
        dispatch(Action.SubToName)
        dispatch(Action.SubToItems)
    },
    executorFactory = coroutineExecutorFactory {
        onAction<Action.SubToName> {
            launch {
                observeActivityNameUc.execute(id = dailyChecklistId)
                    .collect { dispatch(Message.NameUpdate(name = it)) }
            }
        }
        onAction<Action.SubToItems> {
            launch {
                observeDailyChecklistItemsUc.execute(dailyChecklistId = dailyChecklistId)
                    .collect { dispatch(Message.ItemsUpdate(items = it)) }
            }
        }
        onIntent<Intent.ToggleItem> { intent ->
            val item = state().items.find { it.id == intent.id }
                ?: return@onIntent // TODO: logging
            if (item.checkedTime == null) {
                launch {
                    checkDailyChecklistItemUc.execute(dailyChecklistItemId = intent.id)
                        ?.let { publish(Label.Error(it)) }
                }
            } else {
                publish(Label.ConfirmUncheckingItem(id = intent.id))
            }
        }
        onIntent<Intent.EditItem> { intent ->
            publish(Label.EditCheckingTime(id = intent.id))
        }
        onIntent<Intent.ItemUncheckConfirmed> { intent ->
            val checkedTime = state().items.find { it.id == intent.id }?.checkedTime
                ?: return@onIntent // TODO: logging
            launch {
                uncheckDailyChecklistItemUc
                    .execute(
                        dailyChecklistItemId = intent.id,
                        time = checkedTime,
                    )
                    ?.let { publish(Label.Error(it)) }
            }
        }
        onIntent<Intent.UpdateItemsTime> { intent ->
            launch {
                updateDailyChecklistCheckTimeUc
                    .execute(
                        dailyChecklistItemId = intent.itemId,
                        oldTime = intent.oldTime,
                        newTime = intent.newTime,
                    )
                    ?.let { publish(Label.Error(it)) }
            }
        }
    },
    reducer = { message: Message -> when (message) {
        is Message.NameUpdate -> copy(name = message.name)
        is Message.ItemsUpdate -> copy(items = message.items)
    } },
) {}.also { store ->
    stateKeeper?.register(
        key = State::class.qualifiedName!!,
        strategy = State.serializer(),
    ) { DailyChecklistStore.INITIAL_STATE }
}
