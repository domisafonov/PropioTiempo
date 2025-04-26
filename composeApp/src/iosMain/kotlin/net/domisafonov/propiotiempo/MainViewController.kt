package net.domisafonov.propiotiempo

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.domisafonov.propiotiempo.component.RootComponentImpl
import net.domisafonov.propiotiempo.ui.RootContent

fun MainViewController() = ComposeUIViewController {
    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle()),
        storeFactory = DefaultStoreFactory(),
    )
    RootContent(
        rootComponent = rootComponent,
    )
}
