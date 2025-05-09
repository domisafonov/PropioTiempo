package net.domisafonov.propiotiempo.component.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.domisafonov.propiotiempo.component.dialog.DialogContainer.ConfirmationResult
import net.domisafonov.propiotiempo.ui.content.dialog.ConfirmationDialogViewModel

interface ConfirmationDialogComponent : DialogComponent {

    val viewModel: StateFlow<ConfirmationDialogViewModel>

    fun onConfirm()
    fun onCancel()
    fun onDismiss()
}

fun makeConfirmationDialogComponent(
    componentContext: ComponentContext,
    mainDispatcher: CoroutineDispatcher,
    onResult: suspend (ConfirmationResult) -> Unit,
    title: String?,
    message: String?,
    okText: String?,
    cancelText: String?,
): ConfirmationDialogComponent = ConfirmationDialogComponentImpl(
    componentContext = componentContext,
    mainDispatcher = mainDispatcher,
    onResult = onResult,
    title = title,
    message = message,
    okText = okText,
    cancelText = cancelText,
)

private class ConfirmationDialogComponentImpl(
    componentContext: ComponentContext,
    mainDispatcher: CoroutineDispatcher,
    private val onResult: suspend (ConfirmationResult) -> Unit,
    title: String?,
    message: String?,
    okText: String?,
    cancelText: String?,
) : ConfirmationDialogComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    override val viewModel: StateFlow<ConfirmationDialogViewModel> =
        MutableStateFlow(
            ConfirmationDialogViewModel(
                title = title,
                message = message,
                okText = okText,
                cancelText = cancelText,
            )
        )

    override fun onConfirm() {
        scope.launch { onResult(ConfirmationResult.Confirmed) }
    }

    override fun onCancel() {
        scope.launch { onResult(ConfirmationResult.Cancelled) }
    }

    override fun onDismiss() {
        scope.launch { onResult(ConfirmationResult.Dismissed) }
    }
}
