package moe.lava.banksia.ui.platform.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.Flow
import moe.lava.banksia.ui.BanksiaEvent
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.util.BoxedValue
import moe.lava.banksia.util.Point

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Int {
    return LocalWindowInfo.current.containerSize.height
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Maps(
    modifier: Modifier,
    state: MapState,
    onEvent: (BanksiaEvent) -> Unit,
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
) {
    TODO("Not yet implemented")
}
