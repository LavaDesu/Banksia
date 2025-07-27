package moe.lava.banksia.native.maps

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.Flow
import moe.lava.banksia.ui.BoxedValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Int {
    return LocalWindowInfo.current.containerSize.height
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Maps(
    modifier: Modifier,
    markers: List<Marker>,
    polylines: List<Polyline>,
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
) {
    TODO("Not yet implemented")
}
