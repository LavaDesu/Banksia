package moe.lava.banksia.ui

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
import moe.lava.banksia.api.ptv.structures.ComposableRouteIcon
import moe.lava.banksia.ui.state.InfoPanelState

@Composable
fun InfoPanel(
    state: InfoPanelState,
    onEvent: (BanksiaEvent) -> Unit,
    onPeekHeightChange: (Dp) -> Unit,
) {
    if (state is InfoPanelState.None)
        return

    val localDensity = LocalDensity.current

    Column(
        Modifier
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
                else -> throw UnsupportedOperationException()
            }

            if (state.loading)
                CircularProgressIndicator(
                    modifier = Modifier.width(32.dp).align(Alignment.CenterEnd)
                )
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeContent))
    }
}

@Composable
private fun RouteInfoPanel(
    state: InfoPanelState.Route,
    onEvent: (BanksiaEvent) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            ComposableRouteIcon(state.type)
            Text(
                state.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun StopInfoPanel(
    state: InfoPanelState.Stop,
    onEvent: (BanksiaEvent) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            state.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start
        )
        state.subname?.let {
            Text(
                "/ $it",
                modifier = Modifier.padding(start = 5.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
        state.departures?.let {
            Spacer(Modifier.height(5.dp))
            it.forEach { (name, formatted) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(formatted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 5.dp))
                }
            }
        }
    }
}
