package moe.lava.banksia.native.maps

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.DefaultMapProperties
import com.google.maps.android.compose.DefaultMapUiSettings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.roundToInt


fun Point.toLatLng(): LatLng = LatLng(this.lat, this.lng)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Maps(
    modifier: Modifier,
    markers: List<Marker>,
    polylines: List<Polyline>,
    cameraPosition: Point,
    sheetState: SheetState,
) {
    val extInsets = if (
        sheetState.currentValue != SheetValue.Hidden ||
        sheetState.targetValue != SheetValue.Hidden
    ) {
        val context = LocalContext.current
        val windowManager = remember { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
        val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            windowManager.currentWindowMetrics.bounds.height()
        else {
            var outMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(outMetrics)
            outMetrics.heightPixels
        }
        val scaffoldOffset = sheetState.requireOffset().roundToInt()
        (screenHeight - scaffoldOffset - WindowInsets.safeDrawing.getBottom(LocalDensity.current)).coerceAtLeast(0)
    } else 0
    var camPos = rememberCameraPositionState()
    LaunchedEffect(cameraPosition) {
        camPos.position = CameraPosition(cameraPosition.toLatLng(), 16.0f, 0.0f, 0.0f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camPos,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        properties = DefaultMapProperties.copy(
            //mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.def_mapstyle),
            //isMyLocationEnabled = checkLocationPermission(),
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
