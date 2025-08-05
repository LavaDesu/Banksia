package moe.lava.banksia.ui.platform.maps

import moe.lava.banksia.util.Point

data class CameraPosition(
    val centre: Point = Point(-37.8136, 144.9631),
    val bounds: CameraPositionBounds? = null,
)
