@file:OptIn(ExperimentalCoroutinesApi::class)

package net.domisafonov.propiotiempo.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun getDayStart(): Instant {
    val timezone = TimeZone.currentSystemDefault()
    return Clock.System.now()
        .toLocalDateTime(timeZone = timezone)
        .date
        .atTime(LocalTime.fromSecondOfDay(0))
        .toInstant(timezone)
}

fun<T : Any> resetAtMidnight(flowProvider: () -> Flow<T>): Flow<T> =
    dailyTimer(LocalTime.fromSecondOfDay(0))
        .flatMapLatest { flowProvider() }

fun dailyTimer(startAt: LocalTime, emitAtStart: Boolean = true): Flow<Unit> = flow {
    if (emitAtStart) {
        emit(Unit)
    }
    while (true) {
        delay(millisToDayEnd(dayTime = startAt))
        emit(Unit)
    }
}

private fun millisToDayEnd(dayTime: LocalTime): Long {
    val millisecondOfToday = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .toMillisecondOfDay()
    val diff = millisecondOfToday - dayTime.toMillisecondOfDay()
    return if (diff > 0) {
        diff
    } else {
        diff + 86400000
    }.toLong()
}
