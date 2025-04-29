package moe.lava.banksia.api.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PtvDirection(
    @SerialName("direction_id") val directionId: Int,
    @SerialName("direction_name") val directionName: String,
    @SerialName("route_id") val routeId: Int,
)
