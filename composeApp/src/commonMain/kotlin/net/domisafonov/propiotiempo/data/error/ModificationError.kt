package net.domisafonov.propiotiempo.data.error

class ModificationError(
    cause: Exception,
    message: String? = null,
) : PtError(
    message = message,
    cause = cause,
)
