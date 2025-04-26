package net.domisafonov.propiotiempo.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.domisafonov.propiotiempo.component.ActivitiesComponent

@Composable
fun ActivitiesContent(modifier: Modifier = Modifier, component: ActivitiesComponent) {
    Text(
        modifier = modifier.windowInsetsPadding(WindowInsets.safeContent),
        text = "aaaaaaa",
    )
}
