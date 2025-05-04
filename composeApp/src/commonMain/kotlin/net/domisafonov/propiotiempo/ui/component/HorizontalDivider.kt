package net.domisafonov.propiotiempo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Box(modifier = modifier.fillMaxWidth().background(color = color))
}
