package net.domisafonov.propiotiempo.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    minSurfaceContentsSize: Dp? = null,
    content: @Composable () -> Unit,
) {
    val modifierWithSize = minSurfaceContentsSize
        ?.let { modifier.defaultMinSize(minHeight = minSurfaceContentsSize + 16.dp) }
        ?: modifier
    Surface(
        modifier = modifierWithSize
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal)
                    .union(WindowInsets(4.dp, 0.dp, 4.dp, 0.dp))
            )
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}
