package net.domisafonov.propiotiempo.data.db

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant

object InstantLongAdapter : ColumnAdapter<Instant, Long> {
    override fun encode(value: Instant): Long = value.epochSeconds
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochSeconds(databaseValue)
}
