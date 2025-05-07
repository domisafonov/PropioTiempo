package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.domisafonov.propiotiempo.data.model.PtSettings
import net.domisafonov.propiotiempo.data.repository.SettingsRepository

interface GetSettingsUc {
    suspend fun execute(): PtSettings
}

class GetSettingsUcImpl(
    settingsRepositoryProvider: Lazy<SettingsRepository>,
) : GetSettingsUc {

    private val settingsRepository by settingsRepositoryProvider

    override suspend fun execute(): PtSettings = withContext(Dispatchers.IO) {
        settingsRepository.settings.value
    }
}
