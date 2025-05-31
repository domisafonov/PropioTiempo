package net.domisafonov.propiotiempo.component.dialog

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
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import net.domisafonov.propiotiempo.component.dialog.DialogContainer.EditTimeResult
import net.domisafonov.propiotiempo.ui.content.dialog.EditTimeDialogViewModel
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.Intent
import net.domisafonov.propiotiempo.ui.store.dialog.EditTimeDialogStore.State
import net.domisafonov.propiotiempo.ui.store.dialog.initialState
import net.domisafonov.propiotiempo.ui.store.dialog.makeEditTimeDialogStore

interface EditTimeDialogComponent : DialogComponent, EditTimeDialogComponent.Callbacks {

    val viewModel: StateFlow<EditTimeDialogViewModel>

    interface Callbacks {

        fun onDismiss()
        fun onConfirm()
        fun onCancel()

        fun onHourUpdate(hour: Int)
        fun onMinuteUpdate(minute: Int)
    }
}

fun makeEditTimeDialogComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    mainDispatcher: CoroutineDispatcher,
    onResult: suspend (EditTimeResult) -> Unit,
    title: String,
    time: LocalTime,
    timeRange: ClosedRange<LocalTime>,
): EditTimeDialogComponent = EditTimeDialogComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    mainDispatcher = mainDispatcher,
    onResult = onResult,
    title = title,
    time = time,
    timeRange = timeRange,
)

private class EditTimeDialogComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    mainDispatcher: CoroutineDispatcher,
    private val onResult: suspend (EditTimeResult) -> Unit,
    private val title: String,
    private val time: LocalTime,
    timeRange: ClosedRange<LocalTime>,
) : EditTimeDialogComponent, ComponentContext by componentContext {

    init {
        require(time in timeRange)
    }

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val store = instanceKeeper.getStore(key = EditTimeDialogStore::class) {
        storeFactory.makeEditTimeDialogStore(
            stateKeeper = stateKeeper,
            initialState = EditTimeDialogStore.initialState(
                title = title,
                time = time,
                timeRange = timeRange,
            ),
        )
    }

    override val viewModel: StateFlow<EditTimeDialogViewModel> = store.states
        .map(this::mapToViewModel)
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = mapToViewModel(
                EditTimeDialogStore.initialState(
                    title = title,
                    time = time,
                    timeRange = timeRange,
                ),
            ),
        )

    private fun mapToViewModel(
        state: State,
    ): EditTimeDialogViewModel = EditTimeDialogViewModel(
        title = state.title,
        time = state.time,
        timeRange = state.timeRange,
    )

    override fun onDismiss() {
        scope.launch { onResult(EditTimeResult.Dismissed) }
    }

    override fun onConfirm() {
        scope.launch { onResult(EditTimeResult.Confirmed(time = store.state.time)) }
    }

    override fun onCancel() {
        scope.launch { onResult(EditTimeResult.Cancelled) }
    }

    override fun onHourUpdate(hour: Int) {
        require(hour in 0..24)
        store.accept(Intent.UpdateHour(hour))
    }

    override fun onMinuteUpdate(minute: Int) {
        require(minute in 0..59)
        store.accept(Intent.UpdateMinute(minute))
    }
}
