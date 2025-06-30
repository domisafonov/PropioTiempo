package net.domisafonov.propiotiempo.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "net.domisafonov.propiotiempo.data.model.Serializers.InstantSerializer",
    ) {
        element<Long>(elementName = "epochSeconds")
        element<Int>(elementName = "nanosecondsOfSecond")
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value = value.epochSeconds)
        encoder.encodeInt(value = value.nanosecondsOfSecond)
    }

    override fun deserialize(decoder: Decoder): Instant {
        val seconds = decoder.decodeLong()
        val nanos = decoder.decodeInt()
        return Instant.fromEpochSeconds(
            epochSeconds = seconds,
            nanosecondAdjustment = nanos,
        )
    }
}
