package moe.lava.banksia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.lava.banksia.api.ptv.structures.PtvRouteType
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
import org.jetbrains.compose.ui.tooling.preview.Preview

data class RouteTypeProperties(
    val colour: Color,
    val drawable: DrawableResource,
    val background: DrawableResource,
    val icon: DrawableResource,
)

fun PtvRouteType.getUIProperties(): RouteTypeProperties {
    val colour = when (this) {
        PtvRouteType.TRAIN -> Color(0xFF0072CE)
        PtvRouteType.TRAM -> Color(0xFF78BE20)
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Color(0xFFFF8200)
        PtvRouteType.VLINE -> Color(0xFF8F1A95)
    }
    val (drawable, background, icon) = when (this) {
        PtvRouteType.TRAM -> Triple(
            Res.drawable.tram, Res.drawable.tram_background, Res.drawable.tram_icon
        )
        PtvRouteType.TRAIN, PtvRouteType.VLINE -> Triple(
            Res.drawable.train, Res.drawable.train_background, Res.drawable.train_icon
        )
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Triple(
            Res.drawable.bus, Res.drawable.bus_background, Res.drawable.bus_icon
        )
    }
    return RouteTypeProperties(colour, drawable, background, icon)
}

@Composable
fun RouteIcon(
    modifier: Modifier = Modifier.Companion,
    size: Dp = 40.dp,
    routeType: PtvRouteType,
) {
    val properties = routeType.getUIProperties()
    Image(
        painter = painterResource(properties.icon),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .padding(size * ICON_PADDING / 2)
            .drawBehind {
                drawCircle(properties.colour, radius = size.toPx() / 2f)
            }
    )
}

const val ICON_PADDING = 0.25f

@Preview
@Composable
private fun RouteIconPreview() {
    Row {
        RouteIcon(routeType = PtvRouteType.TRAIN)
        RouteIcon(routeType = PtvRouteType.TRAM)
        RouteIcon(routeType = PtvRouteType.BUS)
    }
}

