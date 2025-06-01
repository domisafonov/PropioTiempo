package net.domisafonov.propiotiempo.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Stable
class KeyedDropdownMenuState<K: Any> private constructor(
    initialItemShowingMenu: K?,
    private val comparisonKey: (K) -> Any,
) {
    constructor(
        comparisonKey: (K) -> Any,
    ) : this(
        initialItemShowingMenu = null,
        comparisonKey = comparisonKey,
    )

    var itemShowingMenu: K? by mutableStateOf(initialItemShowingMenu)
        private set

    fun requestMenu(key: K) {
        itemShowingMenu = key
    }

    fun hideMenu() {
        itemShowingMenu = null
    }

    fun isShowingMenuForKey(key: K): Boolean = itemShowingMenu
        ?.let { currentKey -> comparisonKey(currentKey) == comparisonKey(key) } == true
}

@Composable
@Suppress("UNCHECKED_CAST")
fun<K : Any> rememberKeyedDropdownMenuState(
   comparisonKey: (K) -> Any = { it },
): KeyedDropdownMenuState<K> =
    remember { KeyedDropdownMenuState<K>(comparisonKey = comparisonKey) }

@Composable
fun<K : Any> KeyedDropdownMenu(
    modifier: Modifier = Modifier,
    state: KeyedDropdownMenuState<K>,
    key: K,
    content: @Composable (
        onDismissRequest: () -> Unit,
        isExpanded: Boolean,
    ) -> Unit,
) {
    Box(modifier = modifier) {
        content(
            state::hideMenu,
            state.isShowingMenuForKey(key),
        )
    }
}
