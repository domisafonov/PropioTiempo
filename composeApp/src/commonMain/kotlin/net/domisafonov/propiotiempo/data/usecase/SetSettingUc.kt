package net.domisafonov.propiotiempo.data.usecase

import net.domisafonov.propiotiempo.data.model.PtSettings
import net.domisafonov.propiotiempo.data.repository.SettingsRepository
import kotlin.reflect.KProperty1

interface SetSettingUc {
    suspend fun<T : Any> execute(
        property: KProperty1<PtSettings, T>,
        value: T,
    )
}

class SetSettingUcImpl(
    settingsRepositoryProvider: Lazy<SettingsRepository>,
) : SetSettingUc {

    private val settingsRepository by settingsRepositoryProvider

    override suspend fun <T : Any> execute(
        property: KProperty1<PtSettings, T>,
        value: T,
    ) {
        settingsRepository.updateSetting(property = property, value = value)
    }
}
