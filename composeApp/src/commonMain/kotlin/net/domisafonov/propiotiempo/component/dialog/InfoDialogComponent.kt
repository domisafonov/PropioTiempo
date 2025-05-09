package net.domisafonov.propiotiempo.component.dialog

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface InfoDialogComponent : DialogComponent

fun makeInfoDialogComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    title: String?,
    message: String,
    onResult: suspend (DialogContainer.InfoResult) -> Unit,
): InfoDialogComponent = InfoDialogComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    title = title,
    message = message,
    onResult = onResult,
)

private class InfoDialogComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    title: String?,
    message: String,
    onResult: suspend (DialogContainer.InfoResult) -> Unit,
) : InfoDialogComponent, ComponentContext by componentContext {

}
