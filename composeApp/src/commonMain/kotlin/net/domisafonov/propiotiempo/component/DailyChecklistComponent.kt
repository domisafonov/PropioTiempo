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
import net.domisafonov.propiotiempo.component.dialog.showEditTimeDialog
import net.domisafonov.propiotiempo.component.dialog.showErrorDialog
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.toLocalTime
import net.domisafonov.propiotiempo.data.usecase.CheckDailyChecklistItemUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveDailyChecklistItemsUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUcImpl
import net.domisafonov.propiotiempo.data.usecase.UncheckDailyChecklistItemUcImpl
import net.domisafonov.propiotiempo.data.usecase.UpdateDailyChecklistCheckTimeUcImpl
import net.domisafonov.propiotiempo.ui.content.DailyChecklistViewModel
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Intent
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.Label
import net.domisafonov.propiotiempo.ui.store.DailyChecklistStore.State
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.ui.store.makeDailyChecklistStore
import org.jetbrains.compose.resources.getString
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.daily_checklist_item_ordinal
import propiotiempo.composeapp.generated.resources.daily_checklist_item_uncheck_confirmation
import propiotiempo.composeapp.generated.resources.edit_time_dialog_title
import kotlin.time.Clock

interface DailyChecklistComponent : ComponentContext, DailyChecklistComponentCallbacks {

    val viewModel: StateFlow<DailyChecklistViewModel>
}

interface DailyChecklistComponentCallbacks {
    fun onNavigateBack()
    fun onItemClick(id: Long)
    fun onItemLongClick(id: Long)
}

fun makeDailyChecklistComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    clock: Clock,
    dailyChecklistId: Long,
    dialogContainer: DialogContainer,
    navigateBack: () -> Unit,
): DailyChecklistComponent = DailyChecklistComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    clock = clock,
    dailyChecklistId = dailyChecklistId,
    dialogContainer = dialogContainer,
    navigateBack = navigateBack,
)

private class DailyChecklistComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    clock: Clock,
    dailyChecklistId: Long,
    dialogContainer: DialogContainer,
    private val navigateBack: () -> Unit,
) : DailyChecklistComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = DailyChecklistStore::class) {
        storeFactory.makeDailyChecklistStore(
            stateKeeper = stateKeeper,
            observeActivityNameUc = ObserveActivityNameUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            observeDailyChecklistItemsUc = ObserveDailyChecklistItemsUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            checkDailyChecklistItemUc = CheckDailyChecklistItemUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            uncheckDailyChecklistItemUc = UncheckDailyChecklistItemUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            updateDailyChecklistCheckTimeUc = UpdateDailyChecklistCheckTimeUcImpl(
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
                is Label.EditCheckingTime -> {
                    val (itemIdx, item) = store.state
                        .items
                        .indexOfFirst { it.id == label.id }
                        .takeIf { it != -1 }
                        ?.let { it to store.state.items[it] }
                        ?: return@collect // TODO: null logging
                    val checkedTime = item.checkedTime
                        ?: return@collect // TODO: disable longclick in view, null logging
                    val res = dialogContainer
                        .showEditTimeDialog(
                            title = getString(
                                Res.string.edit_time_dialog_title,
                                item.name ?: getString(
                                    Res.string.daily_checklist_item_ordinal,
                                    itemIdx + 1
                                )
                            ),
                            time = checkedTime.toLocalTime(),
                        )
                        as? DialogContainer.EditTimeResult.Confirmed
                        ?: return@collect
                    store.accept(
                        Intent.UpdateItemsTime(
                            itemId = label.id,
                            oldTime = checkedTime,
                            newTime = res.time,
                        )
                    )
                }
                is Label.Error -> dialogContainer.showErrorDialog(message = label.inner.message)
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

    override fun onNavigateBack() {
        navigateBack()
    }

    override fun onItemClick(id: Long) {
        store.accept(Intent.ToggleItem(id = id))
    }

    override fun onItemLongClick(id: Long) {
        store.accept(Intent.EditItem(id = id))
    }
}
