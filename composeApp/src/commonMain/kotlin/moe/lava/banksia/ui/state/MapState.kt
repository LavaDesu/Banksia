package moe.lava.banksia.ui.state

import moe.lava.banksia.ui.platform.maps.Marker
import moe.lava.banksia.ui.platform.maps.Polyline

data class MapState(
    val stops: List<Marker.Stop> = listOf(),
    val vehicles: List<Marker.Vehicle> = listOf(),
    val polylines: List<Polyline> = listOf(),
)
