package moe.lava.banksia.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val routeId: String,
    val serviceId: String,
    val shapeId: String?,
    val tripHeadsign: String,
    val directionId: String,
    val blockId: String,
    val wheelchairAccessible: String,
)
