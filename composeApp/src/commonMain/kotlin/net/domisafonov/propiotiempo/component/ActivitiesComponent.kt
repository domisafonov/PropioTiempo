package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.usecase.GetSettingsUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveTodaysChecklistSummaryUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveTodaysTimeActivitySummaryUcImpl
import net.domisafonov.propiotiempo.data.usecase.SetSettingUcImpl
import net.domisafonov.propiotiempo.data.usecase.ToggleTimedActivityUcImpl
import net.domisafonov.propiotiempo.ui.store.makeActivitiesStore
import net.domisafonov.propiotiempo.ui.content.ActivitiesViewModel

interface ActivitiesComponent : ComponentContext {

    val viewModel: StateFlow<ActivitiesViewModel>

    fun onDailyChecklistToggled()
    fun onTimedActivitiesToggled()
    fun onTimedActivityClick(id: Long)
    fun onDailyChecklistClick(id: Long)
}

fun makeActivitiesComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher,
): ActivitiesComponent = ActivitiesComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    ioDispatcher = ioDispatcher,
)

private class ActivitiesComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val activityRepositoryProvider: Lazy<ActivityRepository>,
    private val settingsRepositoryProvider: Lazy<SettingsRepository>,
    private val mainDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
) : ActivitiesComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = ActivitiesStore::class) {
        storeFactory.makeActivitiesStore(
            stateKeeper = stateKeeper,
            observeTodaysChecklistSummaryUc = ObserveTodaysChecklistSummaryUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            observeTodaysTimeActivitySummaryUc = ObserveTodaysTimeActivitySummaryUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            toggleTimedActivityUc = ToggleTimedActivityUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
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

    override val viewModel: StateFlow<ActivitiesViewModel> = store.states
        .map(this::mapToViewModel)
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = mapToViewModel(state = ActivitiesStore.INITIAL_STATE),
        )

    private fun mapToViewModel(
        state: ActivitiesStore.State,
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
        store.accept(ActivitiesStore.Intent.ToggleDailyChecklists)
    }

    override fun onTimedActivitiesToggled() {
        store.accept(ActivitiesStore.Intent.ToggleTimedActivities)
    }

    override fun onTimedActivityClick(id: Long) {
        store.accept(ActivitiesStore.Intent.ClickTimedActivity(id = id))
    }

    override fun onDailyChecklistClick(id: Long) {
        store.accept(ActivitiesStore.Intent.ClickDailyChecklist(id = id))
    }
}
