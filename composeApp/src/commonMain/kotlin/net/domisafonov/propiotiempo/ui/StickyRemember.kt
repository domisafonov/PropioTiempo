package net.domisafonov.propiotiempo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
inline fun stickyRememberBoolean(
    stickyState: Boolean = true,
    crossinline calculation: @DisallowComposableCalls () -> Boolean,
): Boolean {
    var wasInStickyState by remember { mutableStateOf(false) }
    if (wasInStickyState) {
        return stickyState
    }

    val result = calculation()
    if (result == stickyState) {
        wasInStickyState = true
    }
    return result
}
