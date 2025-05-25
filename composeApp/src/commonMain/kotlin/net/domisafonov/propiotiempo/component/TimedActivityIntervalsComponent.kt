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
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveDaysTimedActivityIntervalsUcImpl
import net.domisafonov.propiotiempo.ui.content.TimedActivityIntervalsViewModel
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Intent
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.State
import net.domisafonov.propiotiempo.ui.store.makeTimedActivityIntervalsStore

interface TimedActivityIntervalsComponent : ComponentContext {

    val viewModel: StateFlow<TimedActivityIntervalsViewModel>

    fun onNavigateBack()
    fun onItemClick(startTime: Instant)
    fun onItemLongClick(startTime: Instant)
}

fun makeTimedActivityIntervalsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    timedActivityId: Long,
    dialogContainer: DialogContainer,
    navigateBack: () -> Unit,
): TimedActivityIntervalsComponent = TimedActivityIntervalsComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    timedActivityId = timedActivityId,
    dialogContainer = dialogContainer,
    navigateBack = navigateBack,
)

private class TimedActivityIntervalsComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    timedActivityId: Long,
    dialogContainer: DialogContainer,
    private val navigateBack: () -> Unit,
) : TimedActivityIntervalsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = TimedActivityIntervalsStore::class) {
        storeFactory.makeTimedActivityIntervalsStore(
            stateKeeper = stateKeeper,
            observeActivityNameUc = ObserveActivityNameUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            observeDaysTimedActivityIntervalsUc = ObserveDaysTimedActivityIntervalsUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            timedActivityId = timedActivityId,
        )
    }

    init {
        scope.launch {
            store.labels.collect { label -> when (label) {
                else -> Unit
            } }
        }
    }

    override val viewModel: StateFlow<TimedActivityIntervalsViewModel> = store.states
        .map(this::mapToViewModel)
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = mapToViewModel(TimedActivityIntervalsStore.INITIAL_STATE)
        )

    private fun mapToViewModel(
        state: State,
    ): TimedActivityIntervalsViewModel = TimedActivityIntervalsViewModel(
        x = 1,
    )

    override fun onNavigateBack() {
        navigateBack()
    }

    override fun onItemClick(startTime: Instant) {
        store.accept(Intent.EditInterval(start = startTime))
    }

    override fun onItemLongClick(startTime: Instant) {
        store.accept(Intent.ShowIntervalMenu(start = startTime))
    }
}
