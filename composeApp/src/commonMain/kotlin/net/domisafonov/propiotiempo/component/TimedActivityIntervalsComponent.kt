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
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.component.dialog.showEditTimeDialog
import net.domisafonov.propiotiempo.component.dialog.showErrorDialog
import net.domisafonov.propiotiempo.data.atDateOf
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.toLocalTime
import net.domisafonov.propiotiempo.data.usecase.DeleteTimedActivityIntervalUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveActivityNameUcImpl
import net.domisafonov.propiotiempo.data.usecase.ObserveDaysTimedActivityIntervalsUcImpl
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalStartUcImpl
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalTimeUcImpl
import net.domisafonov.propiotiempo.ui.content.TimedActivityIntervalsViewModel
import net.domisafonov.propiotiempo.ui.store.INITIAL_STATE
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Intent
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.Label
import net.domisafonov.propiotiempo.ui.store.TimedActivityIntervalsStore.State
import net.domisafonov.propiotiempo.ui.store.makeTimedActivityIntervalsStore

interface TimedActivityIntervalsComponent : ComponentContext, TimedActivityIntervalsComponent.Callbacks {

    val viewModel: StateFlow<TimedActivityIntervalsViewModel>

    interface Callbacks {
        fun onNavigateBack()
        fun onItemClick(startTime: Instant)
        fun onItemLongClick(startTime: Instant)
    }
}

fun makeTimedActivityIntervalsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    clock: Clock,
    timedActivityId: Long,
    dialogContainer: DialogContainer,
    navigateBack: () -> Unit,
): TimedActivityIntervalsComponent = TimedActivityIntervalsComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    clock = clock,
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
    clock: Clock,
    timedActivityId: Long,
    dialogContainer: DialogContainer,
    private val navigateBack: () -> Unit,
) : TimedActivityIntervalsComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = TimedActivityIntervalsStore::class) {
        storeFactory.makeTimedActivityIntervalsStore(
            stateKeeper = stateKeeper,
            clock = clock,
            observeActivityNameUc = ObserveActivityNameUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            observeDaysTimedActivityIntervalsUc = ObserveDaysTimedActivityIntervalsUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            updateTimedActivityIntervalStartUc = UpdateTimedActivityIntervalStartUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            updateTimedActivityIntervalTimeUc = UpdateTimedActivityIntervalTimeUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            deleteTimedActivityIntervalUc = DeleteTimedActivityIntervalUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            timedActivityId = timedActivityId,
        )
    }

    init {
        scope.launch {
            store.labels.collect { label -> when (label) {
                is Label.EditIntervalStart -> {
                    val res = dialogContainer
                        .showEditTimeDialog(
                            title = "TODO",
                            time = label.intervalStart.toLocalTime(),
                            onlyLaterThanOrEqual = label.laterThanOrEqual,
                            onlyEarlierThanOrEqual = clock.now().toLocalTime(),
                        )
                        as? DialogContainer.EditTimeResult.Confirmed
                    store.accept(
                        Intent.IntervalStartEditConfirmed(
                            oldStart = label.intervalStart,
                            newStart = res?.time?.atDateOf(label.intervalStart)
                                ?: return@collect,
                        )
                    )
                }
                is Label.EditInterval -> TODO()
                is Label.ShowMenu -> TODO()
                is Label.Error ->
                    dialogContainer.showErrorDialog(message = label.inner.message)
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
    ): TimedActivityIntervalsViewModel = when (state) {
        is State.Ready -> TimedActivityIntervalsViewModel(
            name = state.activityName,
            intervals = state.intervals
                .map { interval ->
                    TimedActivityIntervalsViewModel.Interval(
                        activityId = interval.activityId,
                        start = interval.start,
                        end = interval.end ?: state.currentTime,
                        isActive = interval.end == null,
                    )
                },
        )
        is State.Initializing -> TimedActivityIntervalsViewModel(
            name = "",
            intervals = emptyList(),
        )
    }

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
