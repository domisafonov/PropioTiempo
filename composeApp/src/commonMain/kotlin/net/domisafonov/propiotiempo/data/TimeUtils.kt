@file:OptIn(ExperimentalCoroutinesApi::class)

package net.domisafonov.propiotiempo.data

import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.double_digit_hours_minutes
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

fun getDayStart(): Instant {
    val timezone = TimeZone.currentSystemDefault()
    return Clock.System.now()
        .toLocalDateTime(timeZone = timezone)
        .date
        .atTime(LocalTime.fromSecondOfDay(0))
        .toInstant(timezone)
}

fun<T : Any> resetPeriodically(
    dayTime: LocalTime? = LocalTime.fromSecondOfDay(0),
    doResetMinutely: Boolean = false,
    emitAtStart: Boolean = true,
    flowProvider: () -> Flow<T>,
): Flow<T> {
    if (dayTime == null && !doResetMinutely) {
        throw IllegalArgumentException()
    }
    return periodicalTimer(
        startAt = dayTime,
        doResetMinutely = doResetMinutely,
        emitAtStart = emitAtStart,
    ).flatMapLatest { flowProvider() }
}

private fun periodicalTimer(
    startAt: LocalTime?,
    doResetMinutely: Boolean,
    emitAtStart: Boolean,
): Flow<Unit> = flow {
    if (startAt == null && !doResetMinutely) {
        throw IllegalArgumentException()
    }

    if (emitAtStart) {
        emit(Unit)
    }
    while (true) {
        val now = Clock.System.localTime()
        val one = startAt
            ?.let { millisToDayTime(dayTime = startAt, now = now) }
            ?: Long.MAX_VALUE
        val two = if (doResetMinutely) {
            millisToMinuteEnd(now = now)
        } else {
            Long.MAX_VALUE
        }
        delay(min(one, two))
        emit(Unit)
    }
}

private fun millisToDayTime(
    dayTime: LocalTime,
    now: LocalTime = Clock.System.localTime(),
): Long {
    val millisecondOfToday = now.toMillisecondOfDay()
    val diff = millisecondOfToday - dayTime.toMillisecondOfDay()
    return if (diff > 0) {
        86400000 - diff
    } else {
        diff
    }.toLong()
}

private fun millisToMinuteEnd(now: LocalTime = Clock.System.localTime()): Long =
    (60_000_000_000L - (now.second * 1_000_000_000L + now.nanosecond)) /
        1_000_000L

private fun Clock.localTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalTime = now()
    .toLocalDateTime(timeZone = timeZone)
    .time

@Composable
fun formatDurationHoursMinutes(seconds: Int): String {
    seconds.seconds.toComponents { hours, minutes, _, _ ->
        val hs = hours.toString().padStart(length = 2, padChar = '0')
        val ms = minutes.toString().padStart(length = 2, padChar = '0')
        return stringResource(Res.string.double_digit_hours_minutes, hs, ms)
    }
}

@Composable
fun formatInstantHoursMinutes(instant: Instant): String {
    val time = instant.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val hs = time.hour.toString().padStart(length = 2, padChar = '0')
    val ms = time.minute.toString().padStart(length = 2, padChar = '0')
    return stringResource(Res.string.double_digit_hours_minutes, hs, ms)
}
