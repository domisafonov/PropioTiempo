package net.domisafonov.propiotiempo.component.timedactivityintervals

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
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.data.usecase.DeleteTimedActivityIntervalUcImpl
import net.domisafonov.propiotiempo.data.usecase.GetTimedActivityIntervalUcImpl
import net.domisafonov.propiotiempo.data.usecase.UpdateTimedActivityIntervalTimeUcImpl
import net.domisafonov.propiotiempo.ui.content.timedactivityintervals.EditTimedActivityIntervalDialogViewModel
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.EditTimedActivityIntervalDialogStore.State
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.INITIAL_STATE
import net.domisafonov.propiotiempo.ui.store.timedactivityintervals.makeEditTimedActivityIntervalDialogStore

interface EditTimedActivityIntervalDialogComponent : ComponentContext, EditTimedActivityIntervalDialogComponentCallbacks {

    val viewModel: StateFlow<EditTimedActivityIntervalDialogViewModel>
}

interface EditTimedActivityIntervalDialogComponentCallbacks {
    fun onDismiss()
    fun onConfirm()
    fun onCancel()
    fun onDeleteInterval()
}

fun makeEditTimedActivityIntervalDialogComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    clock: Clock,
    timedActivityId: Long,
    editedIntervalStart: Instant,
    onDismiss: () -> Unit,
): EditTimedActivityIntervalDialogComponent = EditTimedActivityIntervalDialogComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    clock = clock,
    timedActivityId = timedActivityId,
    editedIntervalStart = editedIntervalStart,
    onDismiss = onDismiss,
)

private class EditTimedActivityIntervalDialogComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    clock: Clock,
    timedActivityId: Long,
    editedIntervalStart: Instant,
    private val onDismiss: () -> Unit,
) : EditTimedActivityIntervalDialogComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = EditTimedActivityIntervalDialogStore::class) {
        storeFactory.makeEditTimedActivityIntervalDialogStore(
            stateKeeper = stateKeeper,
            clock = clock,
            getTimedActivityIntervalUc = GetTimedActivityIntervalUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            updateTimedActivityIntervalTimeUc = UpdateTimedActivityIntervalTimeUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
                clock = clock,
            ),
            deleteTimedActivityIntervalUc = DeleteTimedActivityIntervalUcImpl(
                activityRepositoryProvider = activityRepositoryProvider,
            ),
            timedActivityId = timedActivityId,
            editedIntervalStart = editedIntervalStart,
        )
    }

    init {
        scope.launch {
            store.labels.collect { label -> when (label) {
                else -> TODO()
            } }
        }
    }

    override val viewModel: StateFlow<EditTimedActivityIntervalDialogViewModel> =
        store.states
            .map(this::mapToViewModel)
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = mapToViewModel(EditTimedActivityIntervalDialogStore.INITIAL_STATE)
            )

    private fun mapToViewModel(
        state: State,
    ): EditTimedActivityIntervalDialogViewModel = when (state) {
        is State.Initializing -> EditTimedActivityIntervalDialogViewModel.Initializing
        is State.Ready -> EditTimedActivityIntervalDialogViewModel.Ready(
            startTime = state.startTime,
            endTime = state.endTime,
            timeRange = TODO()
        )
    }

    override fun onDismiss() {
        onDismiss.invoke()
    }

    override fun onConfirm() {
        store.accept(Intent.ConfirmEdit)
    }

    override fun onCancel() {
        store.accept(Intent.CancelEdit)
    }

    override fun onDeleteInterval() {
        store.accept(Intent.DeleteInterval)
    }
}
