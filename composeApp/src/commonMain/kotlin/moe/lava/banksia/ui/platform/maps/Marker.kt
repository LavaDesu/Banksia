package moe.lava.banksia.ui.platform.maps

import androidx.compose.ui.graphics.Color
import moe.lava.banksia.api.ptv.structures.PtvRouteType

sealed class Marker {
    abstract val point: Point

    data class Stop(
        override val point: Point,
        val id: Int,
        val type: PtvRouteType,
        val colour: Color,
    ) : Marker()

    data class Vehicle(
        override val point: Point,
        val ref: String,
        val type: PtvRouteType,
    ) : Marker()
}
