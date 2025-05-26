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
    require (dayTime != null || doResetMinutely)
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
    require(startAt != null || doResetMinutely)

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

fun Instant.toLocalTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalTime = toLocalDateTime(timeZone = timeZone)
    .time

private fun Clock.localTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalTime = now()
    .toLocalTime(timeZone = timeZone)

@Composable
fun formatDurationHoursMinutes(seconds: Int): String =
    seconds.seconds.toComponents { hours, minutes, _, _ ->
        formatInstantHoursMinutes(hours = hours, minutes = minutes)
    }

@Composable
fun formatInstantHoursMinutes(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val time = instant.toLocalDateTime(timeZone = timeZone).time
    return formatInstantHoursMinutes(hours = time.hour.toLong(), minutes = time.minute)
}

@Composable
fun formatInstantHoursMinutes(
    hours: Long,
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
    require(start <= end)
    return (end - start).toComponents { days, hours, minutes, _, _  ->
        stringResource(
            Res.string.days_and_double_digit_hours_minutes,
            days.toStringTwoDigit(),
            hours.toStringTwoDigit(),
            minutes.toStringTwoDigit(),
        )
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
