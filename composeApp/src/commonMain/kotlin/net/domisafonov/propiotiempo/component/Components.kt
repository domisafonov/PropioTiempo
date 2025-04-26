package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.serialization.Serializable

interface RootComponent : ComponentContext{
    val screenStack: Value<ChildStack<*, Child>>
    val dialogSlot: Value<ChildSlot<*, Dialog>>

    sealed interface Child {
        data class Activities(val component: ActivitiesComponent) : Child
        data class Schema(val component : SchemaComponent) : Child
    }

    sealed interface Dialog
}

interface ActivitiesComponent : ComponentContext
interface SchemaComponent : ComponentContext
interface DialogComponent : ComponentContext

// TODO: first run things
// TODO: details
@Serializable
private sealed interface ScreenConfig {
    @Serializable data object Activities : ScreenConfig
    @Serializable data object Schema : ScreenConfig
}

// TODO
@Serializable
private data class DialogConfig(
    val title: String?,
    val message: String?,
)

class RootComponentImpl private constructor(
    componentContext: ComponentContext,
    private val activities: (ComponentContext) -> ActivitiesComponent,
    private val schema: (ComponentContext) -> SchemaComponent,
) : RootComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        storeFactory: StoreFactory,
    ) : this(
        componentContext = componentContext,
        activities = { componentContext ->
            ActivitiesComponentImpl(
                componentContext = componentContext,
                storeFactory = storeFactory,
            )
        },
        schema = { componentContext ->
            SchemaComponentImpl(
                componentContext = componentContext,
                storeFactory = storeFactory,
            )
        },
    )

    private val screenNavigation = StackNavigation<ScreenConfig>()
    override val screenStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = screenNavigation,
        serializer = ScreenConfig.serializer(),
        key = "ScreenSlot",
        initialConfiguration = ScreenConfig.Activities,
    ) { config, ctx ->
        when (config) {
            is ScreenConfig.Activities -> RootComponent.Child.Activities(activities(ctx))
            is ScreenConfig.Schema -> RootComponent.Child.Schema(schema(ctx))
        }
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialogSlot: Value<ChildSlot<*, RootComponent.Dialog>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        key = "DialogSlot",
    ) { config, ctx ->
        TODO()
    }
}

private class ActivitiesComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
) : ActivitiesComponent, ComponentContext by componentContext

private class SchemaComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
) : SchemaComponent, ComponentContext by componentContext

private class DialogComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
) : DialogComponent, ComponentContext by componentContext
