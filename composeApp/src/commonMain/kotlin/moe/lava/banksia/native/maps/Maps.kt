package moe.lava.banksia.native.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import moe.lava.banksia.api.ptv.structures.PtvRouteType
import moe.lava.banksia.ui.BanksiaEvent
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.util.BoxedValue

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
data class Point(val lat: Double, val lng: Double)
data class Polyline(val points: List<Point>, val colour: Color)

data class CameraPositionBounds(val northeast: Point, val southwest: Point)
data class CameraPosition(
    val centre: Point = Point(-37.8136, 144.9631),
    val bounds: CameraPositionBounds? = null,
)

@Composable
expect fun getScreenHeight(): Int

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun Maps(
    modifier: Modifier = Modifier,
    state: MapState,
    onEvent: (BanksiaEvent) -> Unit,
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
)
