package moe.lava.banksia.native.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class Marker(val name: String)

@Composable
expect fun Maps(
    modifier: Modifier = Modifier,
    markers: List<Marker> = listOf()
)