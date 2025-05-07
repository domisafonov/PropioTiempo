package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.domisafonov.propiotiempo.ui.store.ActivitiesStore
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.data.ActivityRepository
import net.domisafonov.propiotiempo.data.SettingsRepository
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
): ActivitiesComponent = ActivitiesComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
)

private class ActivitiesComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val activityRepositoryProvider: Lazy<ActivityRepository>,
    private val settingsRepositoryProvider: Lazy<SettingsRepository>,
) : ActivitiesComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = instanceKeeper.getStore(key = ActivitiesStore::class) {
        storeFactory.makeActivitiesStore(
            stateKeeper = stateKeeper,
            activityRepository = activityRepositoryProvider.value,
            settingsRepository = settingsRepositoryProvider.value,
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
