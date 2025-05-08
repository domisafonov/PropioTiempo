package net.domisafonov.propiotiempo.component

import app.cash.sqldelight.db.SqlDriver
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.data.repository.ActivityRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.ReportRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.SchemaRepositoryImpl
import net.domisafonov.propiotiempo.data.db.Daily_checklist_checks
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.db.InstantLongAdapter
import net.domisafonov.propiotiempo.data.db.Time_activity_intervals
import net.domisafonov.propiotiempo.data.repository.makeSettingsRepositoryImpl

interface RootComponent : ComponentContext {
    val screenStack: Value<ChildStack<*, Child>>
    val dialogSlot: Value<ChildSlot<*, Dialog>>

    fun onActivitiesSelection()
    fun onSchemaSelection()

    sealed interface Child {
        data class Activities(val component: ActivitiesComponent) : Child
        data class Schema(val component: SchemaComponent) : Child
        data class DailyChecklist(val component: DailyChecklistComponent) : Child
    }

    sealed interface Dialog
}

class RootComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    databaseDriverProvider: Lazy<SqlDriver>,
) : RootComponent, ComponentContext by componentContext {

    private val database by lazy {
        DatabaseSource.Companion(
            driver = databaseDriverProvider.value,
            daily_checklist_checksAdapter = Daily_checklist_checks.Adapter(
                timeAdapter = InstantLongAdapter,
            ),
            time_activity_intervalsAdapter = Time_activity_intervals.Adapter(
                start_timeAdapter = InstantLongAdapter,
                end_timeAdapter = InstantLongAdapter,
            )
        )
    }
    private val activityRepositoryProvider = lazy {
        ActivityRepositoryImpl(database = database, ioDispatcher = Dispatchers.IO)
    }
    private val schemaRepositoryProvider = lazy {
        SchemaRepositoryImpl(database = database)
    }
    private val reportRepositoryProvider = lazy {
        ReportRepositoryImpl(database = database)
    }

    // TODO: the current idea is to load it in a special way on the starting screen
    //  IO on main/composer threads is still not impossible
    private val settingsRepositoryProvider = lazy {
        makeSettingsRepositoryImpl(
            mainDispatcher = Dispatchers.Main.immediate,
            ioDispatcher = Dispatchers.IO,
        )
    }

    private val activitiesComponent = { componentContext: ComponentContext ->
        makeActivitiesComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            activityRepositoryProvider = activityRepositoryProvider,
            settingsRepositoryProvider = settingsRepositoryProvider,
            mainDispatcher = Dispatchers.Main.immediate,
            ioDispatcher = Dispatchers.IO,
            navigateToChecklist = { id ->
                screenNavigation.pushNew(ScreenConfig.DailyChecklist(id = id))
            }
        )
    }
    private val schemaComponent = { componentContext: ComponentContext ->
        makeSchemaComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            schemaRepositoryProvider = schemaRepositoryProvider,
        )
    }
    private val dailyChecklistComponent = { componentContext: ComponentContext, dailyChecklistId: Long ->
        makeDailyChecklistComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            activityRepositoryProvider = activityRepositoryProvider,
            settingsRepositoryProvider = settingsRepositoryProvider,
            mainDispatcher = Dispatchers.Main.immediate,
            ioDispatcher = Dispatchers.IO,
            dailyChecklistId = dailyChecklistId,
        )
    }

    private val screenNavigation = StackNavigation<ScreenConfig>()
    override val screenStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = screenNavigation,
        serializer = ScreenConfig.serializer(),
        key = "ScreenSlot",
        initialConfiguration = ScreenConfig.Activities,

        // TODO: find a way to return to the activities tab, but close the app if we are already there
        handleBackButton = true,
    ) { config, ctx ->
        when (config) {
            is ScreenConfig.Activities -> RootComponent.Child.Activities(activitiesComponent(ctx))

            is ScreenConfig.Schema -> RootComponent.Child.Schema(schemaComponent(ctx))

            is ScreenConfig.DailyChecklist -> RootComponent.Child.DailyChecklist(
                dailyChecklistComponent(ctx, config.id)
            )
        }
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialogSlot: Value<ChildSlot<*, RootComponent.Dialog>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        key = "DialogSlot",
        handleBackButton = true,
    ) { config, ctx ->
        TODO()
    }

    override fun onActivitiesSelection() {
        screenNavigation.replaceAll(ScreenConfig.Activities)
    }

    override fun onSchemaSelection() {
        screenNavigation.replaceAll(ScreenConfig.Schema)
    }
}

// TODO: first run things
// TODO: details
@Serializable
private sealed interface ScreenConfig {

    @Serializable
    data object Activities : ScreenConfig

    @Serializable
    data object Schema : ScreenConfig

    @Serializable
    data class DailyChecklist(
        val id: Long,
    ) : ScreenConfig
}
