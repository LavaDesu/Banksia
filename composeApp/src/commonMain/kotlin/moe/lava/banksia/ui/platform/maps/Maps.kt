package moe.lava.banksia.ui.platform.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import moe.lava.banksia.ui.screens.MapScreenEvent
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.util.BoxedValue
import moe.lava.banksia.util.Point

@Composable
expect fun getScreenHeight(): Int

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun Maps(
    modifier: Modifier = Modifier.Companion,
    state: MapState,
    onEvent: (MapScreenEvent) -> Unit,
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
)
