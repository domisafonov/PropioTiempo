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
    ignoreFirstValue: Boolean = false,
    crossinline calculation: @DisallowComposableCalls () -> Boolean,
): Boolean {
    var wasInStickyState by remember { mutableStateOf(false) }
    if (wasInStickyState) {
        return stickyState
    }

    val wasRunBefore = remember { RunBeforeState(wasRun = false) }
    val result = calculation()
    val isIgnoredFirstRun = ignoreFirstValue && !wasRunBefore.wasRun
    wasRunBefore.wasRun = true
    if (result == stickyState && !isIgnoredFirstRun) {
        wasInStickyState = true
    }
    return result
}

data class RunBeforeState(
    var wasRun: Boolean,
)
