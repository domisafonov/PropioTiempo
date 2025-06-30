package net.domisafonov.propiotiempo

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.domisafonov.propiotiempo.component.RootComponentImpl
import net.domisafonov.propiotiempo.data.db.DatabaseSource
import net.domisafonov.propiotiempo.ui.content.RootContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
        )

        val rootComponent = RootComponentImpl(
            componentContext = defaultComponentContext(),
            storeFactory = DefaultStoreFactory(),
            databaseDriverProvider = {
                val schema = DatabaseSource.Schema.synchronous()
                AndroidSqliteDriver(
                    schema = schema,
                    context = this,
                    name = "propiotiempo.db", // TODO: proper path
                    callback = object : AndroidSqliteDriver.Callback(schema = schema) {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.setForeignKeyConstraintsEnabled(true)
                        }
                    }
                )
            }
        )

        // TODO: fix status bar color not changing on toggling dark mode
        //  until activity is restarted
        enableEdgeToEdge()
        setContent {
            RootContent(rootComponent = rootComponent)
        }
    }
}
