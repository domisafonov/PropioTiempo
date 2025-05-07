package net.domisafonov.propiotiempo.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import net.domisafonov.propiotiempo.data.model.PtSettings
import kotlin.collections.iterator
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface SettingsRepository {

    val settings: StateFlow<PtSettings>

    suspend fun<T : Any> updateSetting(property: KProperty1<PtSettings, T>, value: T)
}

private class PtSettingsImpl : PtSettings {

    companion object {
        val PROPS: Map<String, PropWithType> = listOf(
            PtSettingsImpl::isActivityTabTimedActivitiesSectionOpen to Type.Bool,
            PtSettingsImpl::isActivityTabDailyChecklistsSectionOpen to Type.Bool,
        ).associate { (p, t) ->
            @Suppress("UNCHECKED_CAST")
            p.name to PropWithType(property = p
                as KMutableProperty1<PtSettingsImpl, Any>, type = t)
        }
    }

    override var isActivityTabTimedActivitiesSectionOpen: Boolean = false
    override var isActivityTabDailyChecklistsSectionOpen: Boolean = false

    fun clone(): PtSettingsImpl = PtSettingsImpl().also { ret ->
        for ((_, p) in PROPS) {
            getSet(p.property, this, ret)
        }
    }

    private fun<T: Any> getSet(
        prop: KMutableProperty1<PtSettingsImpl, T>,
        src: PtSettingsImpl,
        dst: PtSettingsImpl,
    ) {
        prop.set(dst, prop.get(src))
    }
}

private data class PropWithType(
    val property: KMutableProperty1<PtSettingsImpl, Any>,
    val type: Type,
)

private class SettingsRepositoryImpl(
    private val settingsSource: Settings,
    initValue: PtSettingsImpl,
    private val mainDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    private val settingsImpl = MutableStateFlow(initValue)
    override val settings: StateFlow<PtSettings> =
        settingsImpl.asStateFlow()

    override suspend fun <T : Any> updateSetting(
        property: KProperty1<PtSettings, T>,
        value: T,
    ) {
        withContext(mainDispatcher) {
            val realProp = PtSettingsImpl.PROPS[property.name]!!.property

            val new = settingsImpl.value.clone()
            realProp.set(new, value)

            settingsImpl.value = new

            updateSource(new)
        }
    }

    private suspend fun updateSource(
        value: PtSettingsImpl,
    ) = withContext(ioDispatcher) {
        for ((name, pwt) in PtSettingsImpl.PROPS) {
            val (p, t) = pwt
            t.setter(settingsSource, name, p.get(value))
        }
    }
}

private fun readFromSource(
    settingsSource: Settings,
): PtSettingsImpl = PtSettingsImpl().apply {
    for ((name, pwt) in PtSettingsImpl.PROPS) {
        val (p, t) = pwt
        t.getter(settingsSource, name)?.let { p.set(this, it) }
    }
}

fun makeSettingsRepositoryImpl(
    mainDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {
    val settingsSource = Settings()
    return SettingsRepositoryImpl(
        settingsSource = settingsSource,
        initValue = readFromSource(settingsSource = settingsSource),
        mainDispatcher = mainDispatcher,
        ioDispatcher = ioDispatcher,
    )
}

private enum class Type(
    val getter: (Settings, String) -> Any?,
    val setter: (Settings, String, Any) -> Unit,
) {
    Bool(getter = { s, n -> s.get<Boolean>(n) }, setter = { s, n, v -> s[n] = v as Boolean }),
    Double(getter = { s, n -> s.get<Double>(n) }, setter = { s, n, v -> s[n] = v as Double }),
    Float(getter = { s, n -> s.get<Float>(n) }, setter = { s, n, v -> s[n] = v as Float }),
    Int(getter = { s, n -> s.get<Int>(n) }, setter = { s, n, v -> s[n] = v as Int }),
    Long(getter = { s, n -> s.get<Long>(n) }, setter = { s, n, v -> s[n] = v as Long }),
    String(getter = { s, n -> s.get<String>(n) }, setter = { s, n, v -> s[n] = v as String }),
}
