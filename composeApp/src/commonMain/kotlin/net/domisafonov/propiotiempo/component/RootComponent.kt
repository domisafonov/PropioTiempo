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
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import net.domisafonov.propiotiempo.component.dialog.ConfirmationDialogComponent
import net.domisafonov.propiotiempo.component.dialog.DialogContainer
import net.domisafonov.propiotiempo.component.dialog.InfoDialogComponent
import net.domisafonov.propiotiempo.component.dialog.makeConfirmationDialogComponent
import net.domisafonov.propiotiempo.component.dialog.makeInfoDialogComponent
import net.domisafonov.propiotiempo.data.repository.ActivityRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.ReportRepositoryImpl
import net.domisafonov.propiotiempo.data.repository.SchemaRepositoryImpl
import net.domisafonov.propiotiempo.data.db.Daily_checklist_checks
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.data.db.InstantLongAdapter
import net.domisafonov.propiotiempo.data.db.Time_activity_intervals
import net.domisafonov.propiotiempo.data.repository.makeSettingsRepositoryImpl
import net.domisafonov.propiotiempo.ui.singleUse

interface RootComponent : ComponentContext, DialogContainer {

    val screenStack: Value<ChildStack<*, Child>>
    val dialogSlot: Value<ChildSlot<*, Dialog>>

    fun onActivitiesSelection()
    fun onSchemaSelection()

    sealed interface Child {
        data class Activities(val component: ActivitiesComponent) : Child
        data class Schema(val component: SchemaComponent) : Child
        data class DailyChecklist(val component: DailyChecklistComponent) : Child
    }

    sealed interface Dialog {
        data class InfoDialog(val component: InfoDialogComponent) : Dialog
        data class ConfirmationDialog(val component: ConfirmationDialogComponent) : Dialog
    }
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
            dailyChecklistId = dailyChecklistId,
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
            storeFactory = storeFactory,
            title = config.title,
            message = config.message,
            onResult = { dismissWithResult(it) },
        )
    }
    private val confirmationDialogComponent = { componentContext: ComponentContext, config: DialogConfig.ConfirmationDialog  ->
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

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialogSlot: Value<ChildSlot<*, RootComponent.Dialog>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
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
    ): DialogContainer.ConfirmationResult? {

        if (dialogSlot.value.child != null) {
            return null
        }

        dialogNavigation.activate(
            DialogConfig.ConfirmationDialog(
                title = title,
                message = message,
                okText = okText,
                cancelText = cancelText,
            ),
        )

        return dismissEvents.receive() as? DialogContainer.ConfirmationResult // TODO: logging of null
    }

    override suspend fun showInfoDialog(
        title: String?,
        message: String,
    ): DialogContainer.InfoResult? {

        if (dialogSlot.value.child != null) {
            return null
        }

        dialogNavigation.activate(
            DialogConfig.InfoDialog(
                title = title,
                message = message,
            )
        )

        return dismissEvents.receive() as? DialogContainer.InfoResult // TODO: logging of null
    }
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
}

@Serializable
private sealed interface DialogConfig {

    @Serializable
    data class InfoDialog(
        val title: String?,
        val message: String,
    ) : DialogConfig

    @Serializable
    data class ConfirmationDialog(
        val title: String?,
        val message: String?,
        val okText: String?,
        val cancelText: String?,
    ) : DialogConfig
}
