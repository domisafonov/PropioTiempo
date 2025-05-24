package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (isDark) {
            darkColors()
        } else {
            lightColors()
        },
    ) {
        content()
    }
}

// TODO: remake the typography with a composition-local when the need arises
val Typography.listHeader: TextStyle get() = h6
val Typography.numericTimeBody: TextStyle get() =
    body1.copy(fontFamily = FontFamily.Monospace)
val Typography.unfilledBody: TextStyle get() = body1.copy(fontWeight = FontWeight.Light)
