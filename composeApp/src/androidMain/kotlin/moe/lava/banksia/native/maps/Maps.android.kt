package moe.lava.banksia.native.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap

@Composable
actual fun Maps(
    modifier: Modifier,
    markers: List<Marker>
) {
    GoogleMap()
}