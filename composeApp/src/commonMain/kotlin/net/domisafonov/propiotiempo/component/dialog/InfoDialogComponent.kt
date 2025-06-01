package net.domisafonov.propiotiempo.component.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.domisafonov.propiotiempo.component.dialog.DialogContainer.InfoResult
import net.domisafonov.propiotiempo.ui.content.dialog.InfoDialogViewModel

interface InfoDialogComponent : DialogComponent, InfoDialogComponentCallbacks {

    val viewModel: StateFlow<InfoDialogViewModel>
}

interface InfoDialogComponentCallbacks {
    fun onOk()
    fun onDismiss()
}

fun makeInfoDialogComponent(
    componentContext: ComponentContext,
    mainDispatcher: CoroutineDispatcher,
    onResult: suspend (InfoResult) -> Unit,
    title: String?,
    message: String,
): InfoDialogComponent = InfoDialogComponentImpl(
    componentContext = componentContext,
    mainDispatcher = mainDispatcher,
    onResult = onResult,
    title = title,
    message = message,
)

private class InfoDialogComponentImpl(
    componentContext: ComponentContext,
    mainDispatcher: CoroutineDispatcher,
    private val onResult: suspend (InfoResult) -> Unit,
    title: String?,
    message: String,
) : InfoDialogComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    override val viewModel: StateFlow<InfoDialogViewModel> =
        MutableStateFlow(
            InfoDialogViewModel(
                title = title,
                message = message,
            )
        ).asStateFlow()

    override fun onOk() {
        scope.launch { onResult(InfoResult.Confirmed) }
    }

    override fun onDismiss() {
        scope.launch { onResult(InfoResult.Dismissed) }
    }
}
