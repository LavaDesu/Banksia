package moe.lava.banksia.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import moe.lava.banksia.ui.components.RouteIcon
import moe.lava.banksia.ui.screens.MapScreenEvent
import moe.lava.banksia.ui.state.InfoPanelState

@Composable
fun InfoPanel(
    state: InfoPanelState,
    onEvent: (MapScreenEvent) -> Unit,
    onPeekHeightChange: (Dp) -> Unit,
) {
    if (state is InfoPanelState.None)
        return

    val localDensity = LocalDensity.current

    Column(
        Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(max = 250.dp)
            .verticalScroll(rememberScrollState())
            .onSizeChanged {
                onPeekHeightChange(with(localDensity) { it.height.toDp().coerceAtMost(250.dp) })
            }
    ) {
        Box {
            when (state) {
                is InfoPanelState.Route -> RouteInfoPanel(state, onEvent)
                is InfoPanelState.Stop -> StopInfoPanel(state, onEvent)
                is InfoPanelState.Run -> RunInfoPanel(state, onEvent)
                is InfoPanelState.None -> throw UnsupportedOperationException()
            }

            if (state.loading)
                CircularProgressIndicator(
                    modifier = Modifier.Companion.width(32.dp).align(Alignment.Companion.CenterEnd)
                )
        }
        Spacer(Modifier.Companion.windowInsetsBottomHeight(WindowInsets.Companion.safeContent))
    }
}

@Composable
private inline fun RouteInfoPanel(
    state: InfoPanelState.Route,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.Companion.fillMaxWidth()) {
        Row {
            RouteIcon(routeType = state.type)
            Text(
                state.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.SemiBold,
                textAlign = TextAlign.Companion.Start
            )
        }
    }
}

@Composable
private inline fun RunInfoPanel(
    state: InfoPanelState.Run,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.Companion.fillMaxWidth()) {
        Row {
            RouteIcon(routeType = state.type)
            Text(
                "${state.direction} via ${state.routeName ?: "..."}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.SemiBold,
                textAlign = TextAlign.Companion.Start
            )
        }
    }
}

@Composable
private inline fun StopInfoPanel(
    state: InfoPanelState.Stop,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.Companion.fillMaxWidth()) {
        Text(
            state.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Companion.SemiBold,
            textAlign = TextAlign.Companion.Start
        )
        state.subname?.let {
            Text(
                "/ $it",
                modifier = Modifier.Companion.padding(start = 5.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Companion.Gray,
                fontWeight = FontWeight.Companion.SemiBold,
                textAlign = TextAlign.Companion.Start
            )
        }
        state.departures?.let {
            Spacer(Modifier.Companion.height(5.dp))
            it.forEach { (name, formatted) ->
                Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Companion.SemiBold
                    )
                    Text(
                        formatted,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                        modifier = Modifier.Companion.padding(horizontal = 5.dp)
                    )
                }
            }
        }
    }
}
