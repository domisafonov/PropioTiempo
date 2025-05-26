package net.domisafonov.propiotiempo.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.domisafonov.propiotiempo.ui.listHeader
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.foldable_fold
import propiotiempo.composeapp.generated.resources.foldable_unfold
import propiotiempo.composeapp.generated.resources.keyboard_arrow_down
import propiotiempo.composeapp.generated.resources.keyboard_arrow_up
import propiotiempo.composeapp.generated.resources.list_header_collapse
import propiotiempo.composeapp.generated.resources.list_header_expand

@Composable
fun FoldableListHeader(
    modifier: Modifier = Modifier,
    isOpen: Boolean = false,
    text: String,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(
                onClickLabel = stringResource(
                    if (isOpen) {
                        Res.string.list_header_collapse
                    } else {
                        Res.string.list_header_expand
                    }
                ),
                onClick = onClick,
            ),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(
                    insets = WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Horizontal)
                        .union(WindowInsets(4.dp, 0.dp, 4.dp, 0.dp)),
                )
                .padding(PaddingValues(4.dp, 12.dp, 0.dp, 12.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = text,
                style = MaterialTheme.typography.listHeader,
            )

            val (icon, desc) = if (isOpen) {
                Res.drawable.keyboard_arrow_up to Res.string.foldable_fold
            } else {
                Res.drawable.keyboard_arrow_down to Res.string.foldable_unfold
            }
            Icon(
                modifier = Modifier
                    .padding(PaddingValues(start = 12.dp)),
                painter = painterResource(icon),
                contentDescription = stringResource(desc),
            )
        }
    }
}
