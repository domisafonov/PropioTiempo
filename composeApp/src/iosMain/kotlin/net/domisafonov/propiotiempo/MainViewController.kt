package net.domisafonov.propiotiempo

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.domisafonov.propiotiempo.component.RootComponentImpl
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.ui.content.RootContent

fun MainViewController() = ComposeUIViewController {
    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle()),
        storeFactory = DefaultStoreFactory(),
        databaseDriverProvider = lazy {
            NativeSqliteDriver(
                schema = DatabaseSource.Schema.synchronous(),
                name = "propiotiempo.db", // TODO: proper path
                onConfiguration = { config ->
                    config.copy(
                        extendedConfig = DatabaseConfiguration.Extended(
                            foreignKeyConstraints = true,
                        ),
                    )
                }
            )
        },
    )
    RootContent(
        rootComponent = rootComponent,
    )
}
