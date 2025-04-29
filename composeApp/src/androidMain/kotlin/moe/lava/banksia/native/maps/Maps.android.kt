package moe.lava.banksia.native.maps

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.DefaultMapProperties
import com.google.maps.android.compose.DefaultMapUiSettings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import moe.lava.banksia.R
import moe.lava.banksia.native.BanksiaTheme

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
    newCameraPosition: Pair<Point, Pair<Point, Point>?>?,
    cameraPositionUpdated: () -> Unit,
    extInsets: WindowInsets,
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
            if (newCameraPosition.second != null) {
                val (northeast, southwest) = newCameraPosition.second!!
                val bounds = LatLngBounds(
                    southwest.toLatLng(),
                    northeast.toLatLng()
                )
                camPos.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1000)
            } else
                camPos.animate(CameraUpdateFactory.newLatLngZoom(newCameraPosition.first.toLatLng(), 16.0f), 1000)
            cameraPositionUpdated()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camPos,
        mapColorScheme = if (isSystemInDarkTheme()) { ComposeMapColorScheme.DARK } else {
            ComposeMapColorScheme.LIGHT },
        properties = DefaultMapProperties.copy(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.def_mapstyle),
            isMyLocationEnabled = checkLocationPermission(),
        ),
        uiSettings = DefaultMapUiSettings.copy(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
        ),
        contentPadding = WindowInsets.safeDrawing.add(extInsets).asPaddingValues()
    ) {
        // [TODO]: Slight lag when routes with many stops such as the 901 bus is set
        for (marker in markers) {
            val state = rememberMarkerState()
            state.position = marker.point.toLatLng()
            MarkerComposable(
                keys = arrayOf(marker.colour),
                state = state,
                onClick = { marker.onClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BanksiaTheme.colors.surface)
                        .border(2.dp, marker.colour, CircleShape)
                )
            }
        }
        for (polyline in polylines) {
            Polyline(
                points = polyline.points.map { it.toLatLng() },
                color = polyline.colour
            )
        }
    }
}
