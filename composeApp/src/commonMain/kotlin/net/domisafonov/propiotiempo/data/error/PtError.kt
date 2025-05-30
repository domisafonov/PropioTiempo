package net.domisafonov.propiotiempo.data.error

sealed class PtError(
    message: String? = null,
    cause: Exception? = null,
) : Exception(
    message,
    cause,
)
