package moe.lava.banksia.api.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Ordinals used for sorting in searcher
enum class GtfsSubType(val value: Int) {
    MetroTrain(2),
    MetroTram(3),
    MetroBus(4),
    RegionalTrain(1),
    RegionalCoach(5),
    RegionalBus(6),
    SkyBus(11),
    Interstate(10),
}

@Serializable
data class Geopath(
    @SerialName("direction_id") val directionId: Int,
    @SerialName("valid_from") val validFrom: String,
    @SerialName("valid_to") val validTo: String,
    @SerialName("paths") val paths: List<String>,
)

@Serializable
data class Route(
    @SerialName("route_type") val routeType: RouteType,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_number") val routeNumber: String,
    @SerialName("route_name") val routeName: String,
    @SerialName("route_gtfs_id") val routeGtfsId: String,
    @SerialName("geopath") val geopath: List<Geopath>,
) {
    fun gtfsSubType(): GtfsSubType? {
        GtfsSubType.entries.forEach {
            if (routeGtfsId.startsWith(it.value.toString()))
                return it
        }
        return null
    }

    fun getShortFullName(): String {
        var res = ""
        if (this.routeNumber != "")
            res += this.routeNumber + " - "
        res += this.routeName.split(" via")[0]
        return res
    }
}

