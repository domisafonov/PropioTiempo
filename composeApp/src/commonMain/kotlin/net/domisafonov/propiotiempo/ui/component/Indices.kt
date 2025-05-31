package net.domisafonov.propiotiempo.ui.component

// TODO: unit tests
class Indices(
    val extras: Int,
    val internalSize: Int,
) {
    init {
        require(extras >= 0)
        require(internalSize >= 0)
    }

    val externalSize: Int get() = internalSize + extras * 2
    val internalRange: IntRange get() = extras until internalSize + extras

    fun isAtStart(externalIdx: Int, includeFirstInternal: Boolean = false): Boolean {
        validateExternal(externalIdx)
        return externalIdx in 0 until extras ||
            (includeFirstInternal && externalIdx == internalRange.first)
    }

    fun isAtEnd(externalIdx: Int, includeLastInternal: Boolean = false): Boolean {
        validateExternal(externalIdx)
        return (externalIdx - extras - internalSize) in 0 until extras ||
            (includeLastInternal && externalIdx == internalRange.last)
    }

    fun isExtra(externalIdx: Int): Boolean {
        return isAtStart(externalIdx) || isAtEnd(externalIdx)
    }

    fun isInner(externalIdx: Int): Boolean {
        validateExternal(externalIdx)
        return !isExtra(externalIdx)
    }

    fun toInternal(externalIdx: Int): Int = toMaybeInternal(externalIdx)
        ?: throw IndexOutOfBoundsException()

    fun toMaybeInternal(externalIdx: Int): Int? = (externalIdx - extras)
        .takeIf(this::isInternalValid)

    fun toExternal(internalIdx: Int): Int {
        validateInternal(internalIdx)
        return (internalIdx + extras)
    }

    fun validateInternal(internalIdx: Int) {
        require(isInternalValid(internalIdx)) { "invalid internal index: $internalIdx" }
    }

    fun validateExternal(externalIdx: Int) {
        require(externalIdx in 0 until externalSize)
    }

    fun idFromInternal(internalIdx: Int): Int = toExternal(internalIdx)

    fun idFromExternal(externalIdx: Int): Int {
        validateExternal(externalIdx)
        return externalIdx
    }

    private fun isInternalValid(internalIdx: Int): Boolean =
        internalIdx in 0 until internalSize

    override fun toString(): String {
        return "Indices(extras=$extras, internalSize=$internalSize, externalSize=$externalSize, internalRange=$internalRange)"
    }
}
