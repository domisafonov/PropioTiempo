package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.flow.StateFlow

interface ChecklistComponent : ComponentContext {

//    val viewModel: StateFlow<ChecklistViewModel>

    //
}

private class ChecklistComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,

)
