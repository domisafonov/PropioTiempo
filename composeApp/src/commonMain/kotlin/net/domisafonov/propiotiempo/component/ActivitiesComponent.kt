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
import net.domisafonov.propiotiempo.ActivitiesStore
import net.domisafonov.propiotiempo.data.ActivityRepository
import net.domisafonov.propiotiempo.makeActivitiesStore
import net.domisafonov.propiotiempo.ui.ActivitiesViewModel

interface ActivitiesComponent : ComponentContext {
    val viewModel: StateFlow<ActivitiesViewModel>
}

fun makeActivitiesComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
): ActivitiesComponent = ActivitiesComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
)

private class ActivitiesComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val activityRepositoryProvider: Lazy<ActivityRepository>,
) : ActivitiesComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val store = instanceKeeper.getStore(key = ActivitiesStore::class) {
        storeFactory.makeActivitiesStore(
            stateKeeper = stateKeeper,
            activityRepository = activityRepositoryProvider.value,
        )
    }

    override val viewModel: StateFlow<ActivitiesViewModel> = store.states
        .map { state ->
            ActivitiesViewModel(
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
                    )
                },
                areDailiesShown = state.isDailyChecklistViewActive,
                areTimeActivitiesShown = state.isTimedActivitiesViewActive,
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = ActivitiesViewModel(
                dailyChecklists = emptyList(),
                timeActivities = emptyList(),
                areDailiesShown = false,
                areTimeActivitiesShown = false,
            ),
        )
}
