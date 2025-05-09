package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.usecase.CheckDailyChecklistItemUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveDailyChecklistItemsUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveDailyChecklistNameUcImpl
import net.domisafonov.propiotiempo.data.usecase.UncheckDailyChecklistItemUcImpl
import net.domisafonov.propiotiempo.ui.content.DailyChecklistViewModel
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Intent
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Label
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.State
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.ui.store.makeDailyChecklistStore
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.daily_checklist_item_uncheck_confirmation

interface DailyChecklistComponent : ComponentContext {

    val viewModel: StateFlow<DailyChecklistViewModel>

    fun onItemClick(id: Long)
    fun onItemLongClick(id: Long)
}

fun makeDailyChecklistComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    dailyChecklistId: Long,
    dialogContainer: DialogContainer,
): DailyChecklistComponent = DailyChecklistComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    dailyChecklistId = dailyChecklistId,
    dialogContainer = dialogContainer,
)

private class DailyChecklistComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    dailyChecklistId: Long,
    dialogContainer: DialogContainer,
) : DailyChecklistComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = DailyChecklistStore::class) {
        storeFactory.makeDailyChecklistStore(
            stateKeeper = stateKeeper,
            observeDailyChecklistNameUc = ObserveDailyChecklistNameUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            observeDailyChecklistItemsUc = ObserveDailyChecklistItemsUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            checkDailyChecklistItemUc = CheckDailyChecklistItemUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            uncheckDailyChecklistItemUc = UncheckDailyChecklistItemUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            dailyChecklistId = dailyChecklistId,
        )
    }

    init {
        scope.launch {
            store.labels.collect { label -> when (label) {
                is Label.ConfirmUncheckingItem -> {
                    val res = dialogContainer.showConfirmationDialog(
                        title = getString(
                            Res.string.daily_checklist_item_uncheck_confirmation
                        ),
                    )
                    if (res == DialogContainer.ConfirmationResult.Confirmed) {
                        store.accept(Intent.ItemUncheckConfirmed(id = label.id))
                    }
                }
            } }
        }
    }

    override val viewModel: StateFlow<DailyChecklistViewModel> = store.states
        .map(this::mapToViewModel)
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = mapToViewModel(DailyChecklistStore.INITIAL_STATE),
        )

    private fun mapToViewModel(
        state: State,
    ): DailyChecklistViewModel = DailyChecklistViewModel(
        name = state.name,
        items = state.items.map { item ->
            DailyChecklistViewModel.Item(
                id = item.id,
                name = item.name,
                checkedTime = item.checkedTime,
            )
        },
    )

    override fun onItemClick(id: Long) {
        store.accept(Intent.ToggleItem(id = id))
    }

    override fun onItemLongClick(id: Long) {
        store.accept(Intent.EditItem(id = id))
    }
}
