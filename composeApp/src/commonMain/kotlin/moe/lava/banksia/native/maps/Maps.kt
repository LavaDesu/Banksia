package moe.lava.banksia.native.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import moe.lava.banksia.api.ptv.structures.PtvRouteType
import moe.lava.banksia.util.BoxedValue

data class Marker(
    val point: Point,
    val data: Data,
    val onClick: () -> Boolean,
) {
    sealed class Data {
        data class Stop(val colour: Color) : Data()
        data class Vehicle(val type: PtvRouteType) : Data()
    }
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
    markers: List<Marker> = listOf(),
    polylines: List<Polyline> = listOf(),
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
)
