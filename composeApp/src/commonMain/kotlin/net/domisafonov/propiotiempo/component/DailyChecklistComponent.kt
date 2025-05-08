package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import net.domisafonov.propiotiempo.data.repository.ActivityRepository
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import net.domisafonov.propiotiempo.ui.content.DailyChecklistViewModel
import kotlin.time.Duration.Companion.hours

interface DailyChecklistComponent : ComponentContext {

    val viewModel: StateFlow<DailyChecklistViewModel>

    //
}

fun makeDailyChecklistComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    activityRepositoryProvider: Lazy<ActivityRepository>,
    settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher,
): DailyChecklistComponent = DailyChecklistComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    activityRepositoryProvider = activityRepositoryProvider,
    settingsRepositoryProvider = settingsRepositoryProvider,
    mainDispatcher = mainDispatcher,
    ioDispatcher = ioDispatcher,
)

private class DailyChecklistComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val activityRepositoryProvider: Lazy<ActivityRepository>,
    private val settingsRepositoryProvider: Lazy<SettingsRepository>,
    mainDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
) : DailyChecklistComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

//    private val store =

    private val viewModelTmp = MutableStateFlow(DailyChecklistViewModel(
        name = "checklist",
        items = listOf(
            DailyChecklistViewModel.Item(id = 1, name = "first", checkedTime = Clock.System.now().minus(5.hours)),
            DailyChecklistViewModel.Item(id = 2, name = "second", checkedTime = Clock.System.now().minus(3.hours)),
            DailyChecklistViewModel.Item(id = 3, name = "third", checkedTime = null),
        ),
    ))
    override val viewModel: StateFlow<DailyChecklistViewModel>
        get() = viewModelTmp.asStateFlow()
}
