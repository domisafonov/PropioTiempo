package net.domisafonov.propiotiempo.ui

val (() -> Unit).singleUse: () -> Unit get() {
    var wasCalled = false
    return {
        if (!wasCalled) {
            this()
        }
        wasCalled = true
    }
}
