@file:OptIn(ExperimentalCoroutinesApi::class)

package net.domisafonov.propiotiempo.data

import androidx.compose.runtime.Composable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import propiotiempo.composeapp.generated.resources.Res
import propiotiempo.composeapp.generated.resources.days_and_double_digit_hours_minutes
import propiotiempo.composeapp.generated.resources.double_digit_hours_minutes
import propiotiempo.composeapp.generated.resources.double_digit_hours_minutes_range
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun getDayStart(clock: Clock): Instant {
    val timezone = TimeZone.currentSystemDefault()
    return clock.now()
        .toLocalDateTime(timeZone = timezone)
        .date
        .atTime(LocalTime.fromSecondOfDay(0))
        .toInstant(timezone)
}

fun<T : Any> resetPeriodically(
    clock: Clock,
    dayTime: LocalTime? = LocalTime.fromSecondOfDay(0),
    doResetMinutely: Boolean = false,
    emitAtStart: Boolean = true,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    flowProvider: () -> Flow<T>,
): Flow<T> {
    require (dayTime != null || doResetMinutely)
    return periodicTimer(
        clock = clock,
        emitAt = dayTime,
        doEmitMinutely = doResetMinutely,
        emitAtStart = emitAtStart,
        timeZone = timeZone,
    ).flatMapLatest { flowProvider() }
}

fun periodicTimer(
    clock: Clock,
    emitAt: LocalTime? = null,
    doEmitMinutely: Boolean = false,
    emitAtStart: Boolean = true,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Flow<Instant> = flow {
    require(emitAt != null || doEmitMinutely)

    var now = clock.now()
    if (emitAtStart) {
        emit(now)
    }
    while (true) {
        val localNow = now.toLocalTime(timeZone = timeZone)
        val one = emitAt
            ?.let { millisToDayTime(dayTime = emitAt, now = localNow) }
            ?: Long.MAX_VALUE
        val two = if (doEmitMinutely) {
            millisToMinuteEnd(now = localNow)
        } else {
            Long.MAX_VALUE
        }
        delay(min(one, two))
        now = clock.now()
        emit(now)
    }
}

private fun millisToDayTime(
    dayTime: LocalTime,
    now: LocalTime,
): Long {
    val millisecondOfToday = now.toMillisecondOfDay()
    val diff = millisecondOfToday - dayTime.toMillisecondOfDay()
    return if (diff > 0) {
        86400000 - diff
    } else {
        diff
    }.toLong()
}

private fun millisToMinuteEnd(now: LocalTime): Long =
    (60_000_000_000L - (now.second * 1_000_000_000L + now.nanosecond)) /
        1_000_000L

fun Instant.toLocalTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalTime = toLocalDateTime(timeZone = timeZone)
    .time

@Composable
fun formatDurationHoursMinutes(seconds: Int): String =
    seconds.seconds.toComponents { hours, minutes, _, _ ->
        // TODO: handle overflow
        formatDurationHoursMinutes(hours = hours.toInt(), minutes = minutes)
    }

@Composable
fun formatDurationHoursMinutes(hours: Int, minutes: Int): String =
   formatInstantHoursMinutes(hours = hours, minutes = minutes)

@Composable
fun formatInstantHoursMinutes(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val time = instant.toLocalDateTime(timeZone = timeZone).time
    return formatInstantHoursMinutes(hours = time.hour, minutes = time.minute)
}

@Composable
fun formatInstantHoursMinutes(
    hours: Int,
    minutes: Int,
): String {
    require(hours in 0 .. 23)
    require(minutes in 0 .. 59)
    return stringResource(
        Res.string.double_digit_hours_minutes,
        hours.toStringTwoDigit(),
        minutes.toStringTwoDigit(),
    )
}

@Composable
fun formatInstantRangeHoursMinutes(
    start: Instant,
    end: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    require(start <= end)
    val startTime = start.toLocalDateTime(timeZone = timeZone)
    val endTime = end.toLocalDateTime(timeZone = timeZone)
    return stringResource(
        Res.string.double_digit_hours_minutes_range,
        startTime.hour.toStringTwoDigit(),
        startTime.minute.toStringTwoDigit(),
        endTime.hour.toStringTwoDigit(),
        endTime.minute.toStringTwoDigit(),
    )
}

@Composable
fun formatDurationDaysHoursMinutes(
    start: Instant,
    end: Instant,
): String {
    require(start <= end) { "interval's start ($start) is after than its end ($end)" }
    return (end - start).toComponents { days, hours, minutes, _, _  ->
        if (days == 0L) {
            formatDurationHoursMinutes(hours = hours, minutes = minutes)
        } else {
            stringResource(
                Res.string.days_and_double_digit_hours_minutes,
                days.toStringTwoDigit(),
                hours.toStringTwoDigit(),
                minutes.toStringTwoDigit(),
            )
        }
    }
}

private fun Int.toStringTwoDigit(): String =
    toString().padStart(length = 2, padChar = '0')
private fun Long.toStringTwoDigit(): String =
    toString().padStart(length = 2, padChar = '0')

fun LocalTime.withHour(hour: Int) =
    LocalTime(
        hour = hour,
        minute = minute,
        second = second,
        nanosecond = nanosecond,
    )

fun LocalTime.withMinute(minute: Int) =
    LocalTime(
        hour = hour,
        minute = minute,
        second = second,
        nanosecond = nanosecond,
    )

fun LocalTime.atDateOf(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) = atDate(instant.toLocalDateTime(timeZone = timeZone).date)
    .toInstant(timeZone = timeZone)

fun LocalTime.Companion.dayStart(): LocalTime = fromSecondOfDay(0)

fun LocalTime.Companion.dayEnd(): LocalTime =
    fromNanosecondOfDay(86400L * 1_000_000_000L - 1L)

fun LocalTime.isSameMinute(other: LocalTime): Boolean =
    hour == other.hour && minute == other.minute
