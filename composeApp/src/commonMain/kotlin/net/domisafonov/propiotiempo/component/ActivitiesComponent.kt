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
import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.component.dialog.showErrorDialog
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.usecase.GetSettingsUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveTodaysChecklistSummaryUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveTodaysTimedActivitySummaryUcImpl
import net.domisafonov.propiotiempo.data.usecase.SetSettingUcImpl
import net.domisafonov.propiotiempo.data.usecase.ToggleTimedActivityUcImpl
import net.domisafonov.propiotiempo.ui.store.makeActivitiesStore
import net.domisafonov.propiotiempo.ui.content.ActivitiesViewModel
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.Intent
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.Label
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore.State

interface ActivitiesComponent : ComponentContext, ActivitiesComponentCallbacks {

    val viewModel: StateFlow<ActivitiesViewModel>
}

interface ActivitiesComponentCallbacks {
    fun onDailyChecklistToggled()
    fun onTimedActivitiesToggled()
    fun onTimedActivityClick(id: Long)
    fun onTimedActivityLongClick(id: Long)
    fun onDailyChecklistClick(id: Long)
}

fun makeActivitiesComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher,
    clock: Clock,
    dialogContainer: DialogContainer,
    navigateToChecklist: (Long) -> Unit,
    navigateToTimedActivityIntervals: (Long) -> Unit,
): ActivitiesComponent = ActivitiesComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    ioDispatcher = ioDispatcher,
    clock = clock,
    dialogContainer = dialogContainer,
    navigateToChecklist = navigateToChecklist,
    navigateToTimedActivityIntervals = navigateToTimedActivityIntervals,
)

private class ActivitiesComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher,
    clock: Clock,
    dialogContainer: DialogContainer,
    navigateToChecklist: (id: Long) -> Unit,
    navigateToTimedActivityIntervals: (id: Long) -> Unit,
) : ActivitiesComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = ActivitiesStore::class) {
        storeFactory.makeActivitiesStore(
            stateKeeper = stateKeeper,
            observeTodaysChecklistSummaryUc = ObserveTodaysChecklistSummaryUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            observeTodaysTimedActivitySummaryUc = ObserveTodaysTimedActivitySummaryUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            toggleTimedActivityUc = ToggleTimedActivityUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            getSettingsUc = GetSettingsUcImpl(
                settingsRepositoryProvider = settingsRepositoryProvider,
                ioDispatcher = ioDispatcher,
            ),
            setSettingsUc = SetSettingUcImpl(
                settingsRepositoryProvider = settingsRepositoryProvider,
            ),
        )
    }

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is Label.NavigateToDailyChecklist -> navigateToChecklist(label.id)
                    is Label.NavigateToTimedActivityIntervals ->
                        navigateToTimedActivityIntervals(label.id)
                    is Label.Error ->
                        dialogContainer.showErrorDialog(message = label.inner.message)
                }
            }
        }
    }

    override val viewModel: StateFlow<ActivitiesViewModel> = store.states
        .map(this::mapToViewModel)
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = mapToViewModel(state = ActivitiesStore.INITIAL_STATE),
        )

    private fun mapToViewModel(
        state: State,
    ): ActivitiesViewModel = ActivitiesViewModel(
        dailyChecklists = state.dailyChecklists.map {
            ActivitiesViewModel.Checklist(
                id = it.id,
                name = it.name,
                isCompleted = it.isCompleted,
            )
        },
        timeActivities = state.timedActivities.map {
            ActivitiesViewModel.TimeActivity(
                id = it.id,
                name = it.name,
                todaysSeconds = it.todaysSeconds,
                isActive = it.isActive,
            )
        },
        areDailiesShown = state.isDailyChecklistViewActive,
        areTimeActivitiesShown = state.isTimedActivitiesViewActive,
    )

    override fun onDailyChecklistToggled() {
        store.accept(Intent.ToggleDailyChecklists)
    }

    override fun onTimedActivitiesToggled() {
        store.accept(Intent.ToggleTimedActivities)
    }

    override fun onTimedActivityClick(id: Long) {
        store.accept(Intent.ToggleTimedActivity(id = id))
    }

    override fun onTimedActivityLongClick(id: Long) {
        store.accept(Intent.OpenTimedActivityIntervals(id = id))
    }

    override fun onDailyChecklistClick(id: Long) {
        store.accept(Intent.ClickDailyChecklist(id = id))
    }
}
