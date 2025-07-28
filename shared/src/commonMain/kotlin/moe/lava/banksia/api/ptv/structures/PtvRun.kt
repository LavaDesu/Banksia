package moe.lava.banksia.api.ptv.structures

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PtvVehiclePosition(
    val latitude: Double,
    val longitude: Double,
    val easting: Double?,
    val northing: Double?,
    val direction: String?,
    val bearing: Double?,
    val supplier: String?,
    @SerialName("datetime_utc") val datetimeUtc: Instant?,
    @SerialName("expiry_time") val expiryTime: Instant?,
)

@Serializable
data class PtvRun(
    @SerialName("run_ref") val runRef: String,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_type") val routeType: PtvRouteType,
    @SerialName("final_stop_id") val finalStopId: Int,
    @SerialName("destination_name") val destinationName: String,
    @SerialName("direction_id") val directionId: Int,
    @SerialName("status") val status: String,
)
