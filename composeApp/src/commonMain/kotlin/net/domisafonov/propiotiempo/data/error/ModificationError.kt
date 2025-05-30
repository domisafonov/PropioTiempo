package net.domisafonov.propiotiempo.data.error

class ModificationError(
    cause: Throwable,
    message: String? = null,
) : Exception(
    message,
    cause,
)
