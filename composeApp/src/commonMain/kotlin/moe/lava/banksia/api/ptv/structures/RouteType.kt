package moe.lava.banksia.api.ptv.structures

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.bus
import moe.lava.banksia.resources.bus_background
import moe.lava.banksia.resources.bus_icon
import moe.lava.banksia.resources.train
import moe.lava.banksia.resources.train_background
import moe.lava.banksia.resources.train_icon
import moe.lava.banksia.resources.tram
import moe.lava.banksia.resources.tram_background
import moe.lava.banksia.resources.tram_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class RouteTypeProperties(
    val colour: Color,
    val drawable: DrawableResource,
    val background: DrawableResource,
    val icon: DrawableResource,
)
fun PtvRouteType.getProperties(): RouteTypeProperties {
    val colour = when (this) {
        PtvRouteType.TRAIN -> Color(0xFF0072CE)
        PtvRouteType.TRAM -> Color(0xFF78BE20)
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Color(0xFFFF8200)
        PtvRouteType.VLINE -> Color(0xFF8F1A95)
    }
    val (drawable, background, icon) = when (this) {
        PtvRouteType.TRAM -> Triple(
            Res.drawable.tram, Res.drawable.tram_background, Res.drawable.tram_icon)
        PtvRouteType.TRAIN, PtvRouteType.VLINE -> Triple(
            Res.drawable.train, Res.drawable.train_background, Res.drawable.train_icon)
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Triple(
            Res.drawable.bus, Res.drawable.bus_background, Res.drawable.bus_icon)
    }
    return RouteTypeProperties(colour, drawable, background, icon)
}

@Composable
fun ComposableRouteIcon(routeType: PtvRouteType) {
    val properties = routeType.getProperties()
    Image(
        painter = painterResource(properties.icon),
        contentDescription = null,
        modifier = Modifier
            .drawBehind {
                drawCircle(properties.colour, radius = (this.size.minDimension + 10.dp.toPx()) / 2f)
            }
    )
}

@Composable
inline fun PtvRouteType.ComposableIcon() = ComposableRouteIcon(this)