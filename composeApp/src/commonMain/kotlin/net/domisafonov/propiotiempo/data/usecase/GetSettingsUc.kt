package net.domisafonov.propiotiempo.data.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.domisafonov.propiotiempo.data.model.PtSettings
import net.domisafonov.propiotiempo.data.repository.SettingsRepository

fun interface GetSettingsUc {
    suspend fun execute(): PtSettings
}

class GetSettingsUcImpl(
    private val settingsRepositoryProvider: Lazy<SettingsRepository>,
    private val ioDispatcher: CoroutineDispatcher,
) : GetSettingsUc {

    override suspend fun execute(): PtSettings =
        if (settingsRepositoryProvider.isInitialized()) {
            get()
        } else {
            withContext(ioDispatcher) { get() }
        }

    private fun get(): PtSettings = settingsRepositoryProvider.value.settings.value
}
