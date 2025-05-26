package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
        colorScheme = if (isDark) {
            darkColorScheme()
        } else {
            lightColorScheme()
        },
    ) {
        content()
    }
}

// TODO: remake the typography with a composition-local when the need arises
val Typography.listHeader: TextStyle get() = headlineSmall
val Typography.numericTimeBody: TextStyle get() =
    bodyMedium.copy(fontFamily = FontFamily.Monospace)
val Typography.unfilledBody: TextStyle get() =
    bodyMedium.copy(fontWeight = FontWeight.Light)
