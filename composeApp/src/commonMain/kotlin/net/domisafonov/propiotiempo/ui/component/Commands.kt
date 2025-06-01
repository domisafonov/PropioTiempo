package net.domisafonov.propiotiempo.ui.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.shareIn

fun<C : Any> commandChannel(): Channel<C> = Channel<C>(
    capacity = 8,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    // TODO: logging of undelivered elements
)

fun<C : Any> Channel<C>.commandFlow(scope: CoroutineScope): SharedFlow<C> =
    consumeAsFlow()
        .shareIn(scope = scope, started = SharingStarted.Lazily)
