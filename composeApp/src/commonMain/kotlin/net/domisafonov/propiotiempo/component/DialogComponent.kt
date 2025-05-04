package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.serialization.Serializable

interface DialogComponent : ComponentContext

// TODO
@Serializable
data class DialogConfig(
    val title: String?,
    val message: String?,
)

fun makeDialogComponent(
    componentContext: ComponentContext,
    storeFactoryProvider: Lazy<StoreFactory>,
): DialogComponent = DialogComponentImpl(
    componentContext = componentContext,
    storeFactoryProvider = storeFactoryProvider,
)

private class DialogComponentImpl(
    componentContext: ComponentContext,
    storeFactoryProvider: Lazy<StoreFactory>,
) : DialogComponent, ComponentContext by componentContext
