package net.domisafonov.propiotiempo.component

import app.cash.sqldelight.db.SqlDriver
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.component.dialog.ConfirmationDialogComponent
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.component.dialog.EditTimeDialogComponent
import net.domisafonov.propiotiempo.component.dialog.InfoDialogComponent
import net.domisafonov.propiotiempo.component.dialog.makeConfirmationDialogComponent
import net.domisafonov.propiotiempo.component.dialog.makeEditTimeDialogComponent
import net.domisafonov.propiotiempo.component.dialog.makeInfoDialogComponent
import net.domisafonov.propiotiempo.component.timedactivityintervals.TimedActivityIntervalsComponent
import net.domisafonov.propiotiempo.component.timedactivityintervals.makeTimedActivityIntervalsComponent
import net.domisafonov.propiotiempo.data.repository.ActivityRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.ReportRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.SchemaRepositoryImpl
import net.domisafonov.propiotiempo.data.db.Daily_checklist_checks
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.db.InstantLongAdapter
import net.domisafonov.propiotiempo.data.db.Time_activity_intervals
import net.domisafonov.propiotiempo.data.repository.makeSettingsRepositoryImpl
import net.domisafonov.propiotiempo.ui.singleUse
import kotlin.time.Clock

interface RootComponent : ComponentContext, DialogContainer, RootComponentCallbacks {

    val screenStack: Value<ChildStack<*, Child>>
    val dialogSlot: Value<ChildSlot<*, Dialog>>

    sealed interface Child {
        data class Activities(val component: ActivitiesComponent) : Child
        data class Schema(val component: SchemaComponent) : Child
        data class DailyChecklist(val component: DailyChecklistComponent) : Child
        data class TimedActivityIntervals(
            val component: TimedActivityIntervalsComponent,
        ) : Child
    }

    sealed interface Dialog {
        data class InfoDialog(val component: InfoDialogComponent) : Dialog
        data class ConfirmationDialog(val component: ConfirmationDialogComponent) : Dialog
        data class EditTimeDialog(val component: EditTimeDialogComponent) : Dialog
    }
}

interface RootComponentCallbacks {
    fun onActivitiesSelection()
    fun onSchemaSelection()
}

class RootComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    databaseDriverProvider: () -> SqlDriver,
) : RootComponent, ComponentContext by componentContext {

    private lateinit var databaseDriver: SqlDriver
    private val database by lazy {
        databaseDriver = databaseDriverProvider()
        DatabaseSource.Companion(
            driver = databaseDriver,
            daily_checklist_checksAdapter = Daily_checklist_checks.Adapter(
                timeAdapter = InstantLongAdapter,
            ),
            time_activity_intervalsAdapter = Time_activity_intervals.Adapter(
                start_timeAdapter = InstantLongAdapter,
                end_timeAdapter = InstantLongAdapter,
            )
        )
    }

    init {
        doOnDestroy {
            if (this::databaseDriver.isInitialized) {
                databaseDriver.close()
            }
        }
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

    // TODO: the current idea is to load it in a special way on the starting screen.
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
            clock = Clock.System,
            dialogContainer = this,
            navigateToChecklist = { id ->
                screenNavigation.pushNew(ScreenConfig.DailyChecklist(id = id))
            },
            navigateToTimedActivityIntervals = { id ->
                screenNavigation.pushNew(ScreenConfig.TimedActivityIntervals(id = id))
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
            clock = Clock.System,
            dailyChecklistId = dailyChecklistId,
            dialogContainer = this,
            navigateBack = { screenNavigation.pop() }.singleUse,
        )
    }
    private val timedActivityIntervalsComponent = { componentContext: ComponentContext, timedActivityId: Long ->
        makeTimedActivityIntervalsComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            activityRepositoryProvider = activityRepositoryProvider,
            settingsRepositoryProvider = settingsRepositoryProvider,
            mainDispatcher = Dispatchers.Main.immediate,
            clock = Clock.System,
            timedActivityId = timedActivityId,
            dialogContainer = this,
            navigateBack = { screenNavigation.pop() }.singleUse,
        )
    }

    // TODO: predictive back?
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

            is ScreenConfig.TimedActivityIntervals -> RootComponent.Child.TimedActivityIntervals(
                timedActivityIntervalsComponent(ctx, config.id)
            )
        }
    }

    override fun onActivitiesSelection() {
        screenNavigation.replaceAll(ScreenConfig.Activities)
    }

    override fun onSchemaSelection() {
        screenNavigation.replaceAll(ScreenConfig.Schema)
    }

    private val infoDialogComponent = { componentContext: ComponentContext, config: DialogConfig.InfoDialog ->
        makeInfoDialogComponent(
            componentContext = componentContext,
            mainDispatcher = Dispatchers.Main.immediate,
            onResult = { dismissWithResult(it) },
            title = config.title,
            message = config.message,
        )
    }
    private val confirmationDialogComponent = { componentContext: ComponentContext, config: DialogConfig.ConfirmationDialog ->
        makeConfirmationDialogComponent(
            componentContext = componentContext,
            mainDispatcher = Dispatchers.Main.immediate,
            onResult = { dismissWithResult(it) },
            title = config.title,
            message = config.message,
            okText = config.okText,
            cancelText = config.cancelText,
        )
    }
    private val editTimeDialogComponent = { componentContext: ComponentContext, config: DialogConfig.EditTimeDialog ->
        makeEditTimeDialogComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            mainDispatcher = Dispatchers.Main.immediate,
            onResult = { dismissWithResult(it) },
            title = config.title,
            time = config.time,
            timeRange = config.timeRange,
        )
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialogSlot: Value<ChildSlot<*, RootComponent.Dialog>> = childSlot(
        source = dialogNavigation,

        // more complex dialogs should rather be implemented per-screen
        serializer = null,

        key = "DialogSlot",
        handleBackButton = true,
    ) { config, ctx ->
        when (config) {
            is DialogConfig.InfoDialog -> RootComponent.Dialog.InfoDialog(
                infoDialogComponent(ctx, config)
            )
            is DialogConfig.ConfirmationDialog -> RootComponent.Dialog.ConfirmationDialog(
                confirmationDialogComponent(ctx, config)
            )
            is DialogConfig.EditTimeDialog -> RootComponent.Dialog.EditTimeDialog(
                editTimeDialogComponent(ctx, config)
            )
        }
    }
    private val dismissEvents = Channel<DialogContainer.DialogResult>()

    private suspend fun dismissWithResult(result: DialogContainer.DialogResult) {
        dismissEvents.send(result)
        dialogNavigation.dismiss()
    }

    override suspend fun showConfirmationDialog(
        title: String?,
        message: String?,
        okText: String?,
        cancelText: String?,
    ): DialogContainer.ConfirmationResult? = showDialog(
        DialogConfig.ConfirmationDialog(
            title = title,
            message = message,
            okText = okText,
            cancelText = cancelText,
        ),
    )

    override suspend fun showInfoDialog(
        title: String?,
        message: String,
    ): DialogContainer.InfoResult? = showDialog(
        DialogConfig.InfoDialog(
            title = title,
            message = message,
        )
    )

    override suspend fun showEditTimeDialog(
        title: String,
        time: LocalTime,
        timeRange: ClosedRange<LocalTime>,
    ): DialogContainer.EditTimeResult? = showDialog(
        DialogConfig.EditTimeDialog(
            title = title,
            time = time.coerceIn(timeRange),
            timeRange = timeRange,
        )
    )

    private suspend inline fun<reified R : DialogContainer.DialogResult> showDialog(
        config: DialogConfig,
    ): R? {

        if (dialogSlot.value.child != null) {
            return null
        }

        dialogNavigation.activate(config)

        return dismissEvents.receive() as? R // TODO: logging of null
    }

    // TODO: first run things
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

        @Serializable
        data class TimedActivityIntervals(
            val id: Long,
        ) : ScreenConfig
    }

    private sealed interface DialogConfig {

        data class InfoDialog(
            val title: String?,
            val message: String,
        ) : DialogConfig

        data class ConfirmationDialog(
            val title: String?,
            val message: String?,
            val okText: String?,
            val cancelText: String?,
        ) : DialogConfig

        data class EditTimeDialog(
            val title: String,
            val time: LocalTime,
            val timeRange: ClosedRange<LocalTime>,
        ) : DialogConfig
    }
}
