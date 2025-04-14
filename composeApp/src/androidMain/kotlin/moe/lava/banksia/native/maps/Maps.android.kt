package moe.lava.banksia.native.maps

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.DefaultMapProperties
import com.google.maps.android.compose.DefaultMapUiSettings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import moe.lava.banksia.R


fun Point.toLatLng(): LatLng = LatLng(this.lat, this.lng)

@Composable
private fun checkLocationPermission() =
    ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
actual fun getScreenHeight(): Int {
    val dp = LocalConfiguration.current.screenHeightDp.dp
    return with(LocalDensity.current) {
        dp.roundToPx()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Maps(
    modifier: Modifier,
    markers: List<Marker>,
    polylines: List<Polyline>,
    newCameraPosition: Point?,
    cameraPositionUpdated: () -> Unit,
    extInsets: Int,
) {
    var camPos = rememberCameraPositionState()
    val ctx = LocalContext.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(ctx) }
    LaunchedEffect(Unit) {
        fusedLocation.lastLocation.addOnSuccessListener {
            if (it != null)
                camPos.position = CameraPosition(LatLng(it.latitude, it.longitude), 16.0f, 0.0f, 0.0f)
        }
    }
    LaunchedEffect(newCameraPosition) {
        if (newCameraPosition != null) {
            camPos.position = CameraPosition(newCameraPosition.toLatLng(), 16.0f, 0.0f, 0.0f)
            cameraPositionUpdated()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camPos,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        properties = DefaultMapProperties.copy(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.def_mapstyle),
            isMyLocationEnabled = checkLocationPermission(),
        ),
        uiSettings = DefaultMapUiSettings.copy(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
        ),
        contentPadding = WindowInsets.safeDrawing.add(WindowInsets(bottom = extInsets)).asPaddingValues()
    ) {
        for (marker in markers) {
            Marker(
                name = marker.name,
                onClick = marker.onClick
            )
        }
        for (polyline in polylines) {
            Polyline(
                points = polyline.points.map { it.toLatLng() },
                color = polyline.colour
            )
        }
    }
}
