package net.domisafonov.propiotiempo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.domisafonov.propiotiempo.component.RootComponentImpl
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.ui.RootContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootComponent = RootComponentImpl(
            componentContext = defaultComponentContext(),
            storeFactory = DefaultStoreFactory(),
            databaseDriverProvider = lazy {
                AndroidSqliteDriver(
                    schema = DatabaseSource.Schema.synchronous(),
                    context = this,
                    name = "propiotiempo.db", // TODO: proper path
                )
            }
        )

        enableEdgeToEdge()
        setContent {
            RootContent(rootComponent = rootComponent)
        }
    }
}
