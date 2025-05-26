package net.domisafonov.propiotiempo.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import net.domisafonov.propiotiempo.data.formatDurationHoursMinutes
import net.domisafonov.propiotiempo.data.formatInstantHoursMinutes
import net.domisafonov.propiotiempo.ui.numericTimeBody

@Composable
fun HourMinuteText(
    modifier: Modifier = Modifier,
    instant: Instant,
) {
    HourMinuteTextFormatted(
        modifier = modifier,
        text = formatInstantHoursMinutes(instant),
    )
}

@Composable
fun HourMinuteText(
    modifier: Modifier = Modifier,
    seconds: Int,
) {
    HourMinuteTextFormatted(
        modifier = modifier,
        text = formatDurationHoursMinutes(seconds),
    )
}

@Composable
fun HourMinuteTextFormatted(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        maxLines = 1,
        text = text,
        style = MaterialTheme.typography.numericTimeBody,
    )
}
