package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.flow.Flow
import net.domisafonov.propiotiempo.data.model.PtSettings
import net.domisafonov.propiotiempo.data.repository.SettingsRepository

fun interface ObserveSettingsUc {
    fun execute(): Flow<PtSettings>
}

class ObserveSettingsUcImpl(
    private val settingsRepository: SettingsRepository,
) : ObserveSettingsUc {

    override fun execute(): Flow<PtSettings> = settingsRepository.settings
}
