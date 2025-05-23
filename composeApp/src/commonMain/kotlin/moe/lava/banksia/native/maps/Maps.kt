package moe.lava.banksia.native.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

enum class MarkerType {
    GENERIC_STOP,
}
data class Marker(
    val point: Point,
    val type: MarkerType,
    val colour: Color,
    val onClick: () -> Boolean
)
data class Point(val lat: Double, val lng: Double)
data class Polyline(val points: List<Point>, val colour: Color)

@Composable
expect fun getScreenHeight(): Int

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun Maps(
    modifier: Modifier = Modifier,
    markers: List<Marker> = listOf(),
    polylines: List<Polyline> = listOf(),
    // <Centre: Point, Bounds?: <Northeast, Southwest>>
    newCameraPosition: Pair<Point, Pair<Point, Point>?>? = Pair(Point(-37.8136, 144.9631), null),
    cameraPositionUpdated: () -> Unit,
    extInsets: WindowInsets,
)
