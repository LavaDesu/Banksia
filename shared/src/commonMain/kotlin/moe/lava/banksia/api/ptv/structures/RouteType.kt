package moe.lava.banksia.api.ptv.structures

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private object RouteTypeSerializer : KSerializer<RouteType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        RouteType::class.qualifiedName!!,
        PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: RouteType) {
        encoder.encodeInt(value.ordinal)
    }

    override fun deserialize(decoder: Decoder): RouteType {
        val index = decoder.decodeInt()
        return RouteType.entries[index]
    }
}

@Serializable(with = RouteTypeSerializer::class)
enum class RouteType {
    TRAIN,
    TRAM,
    BUS,
    VLINE,
    NIGHT_BUS,
}
