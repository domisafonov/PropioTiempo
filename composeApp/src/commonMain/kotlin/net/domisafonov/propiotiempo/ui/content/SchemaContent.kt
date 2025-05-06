package net.domisafonov.propiotiempo.ui.content

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.domisafonov.propiotiempo.component.SchemaComponent

@Composable
fun SchemaContent(modifier: Modifier = Modifier, component: SchemaComponent) {
    Text(
        modifier = modifier.windowInsetsPadding(WindowInsets.safeContent),
        text = "bbbbbbbbb",
    )
}
