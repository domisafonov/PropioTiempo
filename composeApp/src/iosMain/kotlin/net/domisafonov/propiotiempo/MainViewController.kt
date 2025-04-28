package net.domisafonov.propiotiempo

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.domisafonov.propiotiempo.component.RootComponentImpl
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.ui.RootContent

fun MainViewController() = ComposeUIViewController {
    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle()),
        storeFactory = DefaultStoreFactory(),
        databaseDriverProvider = lazy {
            NativeSqliteDriver(
                schema = DatabaseSource.Schema.synchronous(),
                name = "propiotiempo.db", // TODO: proper path
            )
        },
    )
    RootContent(
        rootComponent = rootComponent,
    )
}
