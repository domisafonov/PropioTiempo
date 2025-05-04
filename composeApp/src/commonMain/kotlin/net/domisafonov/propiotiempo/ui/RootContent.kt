package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import net.domisafonov.propiotiempo.component.RootComponent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.nav_activities
import propiotiempo.composeapp.generated.resources.nav_schema
import propiotiempo.composeapp.generated.resources.nav_task

private val TABS = listOf(
    Tab(
        label = Res.string.nav_activities,
        icon = Res.drawable.nav_task,
        onClick = { onActivitiesSelection() },
        isActive = { it is RootComponent.Child.Activities }
    ),
    Tab(
        label = Res.string.nav_schema,
        icon = Res.drawable.nav_schema,
        onClick = { onSchemaSelection() },
        isActive = { it is RootComponent.Child.Schema },
    ),
)

private data class Tab(
    val label: StringResource,
    val icon: DrawableResource,
    val onClick: RootComponent.() -> Unit,
    val isActive: (RootComponent.Child) -> Boolean,
)

@Composable
fun RootContent(modifier: Modifier = Modifier, rootComponent: RootComponent) {
    MaterialTheme {
        Column(modifier = modifier.fillMaxSize()) {
            CurrentScreen(
                modifier = Modifier.weight(1f)
                    .consumeWindowInsets(
                        WindowInsets.safeContent.only(WindowInsetsSides.Bottom)
                    ),
                rootComponent = rootComponent,
            )
            BottomNav(
                modifier = Modifier.fillMaxWidth(),
                rootComponent = rootComponent,
            )
        }
    }
}

@Composable
private fun BottomNav(
    modifier: Modifier = Modifier,
    rootComponent: RootComponent,
) {
    BottomNavigation(
        modifier = modifier,
        windowInsets = WindowInsets.safeContent.only(
            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
        ),
    ) {
        val stack by rootComponent.screenStack.subscribeAsState()
        val currentConfiguration = stack.active.instance
        for (tab in TABS) {
            val label = stringResource(tab.label)
            BottomNavigationItem(
                selected = tab.isActive(currentConfiguration),
                onClick = { tab.onClick(rootComponent) },
                icon = { Icon(painter = painterResource(tab.icon), contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun CurrentScreen(
    modifier: Modifier = Modifier,
    rootComponent: RootComponent,
) {
    val stack by rootComponent.screenStack.subscribeAsState()
    when (val component = stack.active.instance) {
        is RootComponent.Child.Activities -> ActivitiesContent(
            modifier = modifier,
            component = component.component,
        )
        is RootComponent.Child.Schema -> SchemaContent(
            modifier = modifier,
            component = component.component,
        )
    }
}
