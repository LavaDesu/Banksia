package moe.lava.banksia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.native.maps.Maps
import moe.lava.banksia.native.maps.Point
import moe.lava.banksia.native.maps.getScreenHeight
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.my_location_24
import moe.lava.banksia.ui.Searcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    val locationFactory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember { locationFactory.createLocationTracker() }
    BindLocationTrackerEffect(locationTracker)
    var lastLocation by remember { mutableStateOf(Point(-37.8136, 144.9631)) }
    var newCameraPosition by remember { mutableStateOf<Point?>(Point(-37.8136, 144.9631)) }
    var searchTextState by remember { mutableStateOf("") }
    var searchExpandedState by remember { mutableStateOf(false) }

    val sheetState = scaffoldState.bottomSheetState
    val extInsets = if (
        sheetState.currentValue != SheetValue.Hidden ||
        sheetState.targetValue != SheetValue.Hidden
    ) {
        val offset = runCatching { sheetState.requireOffset() }
        val scaffoldOffset = offset.getOrDefault(0.0f).roundToInt()
        (getScreenHeight() - scaffoldOffset - WindowInsets.safeDrawing.getBottom(LocalDensity.current)).coerceAtLeast(0)
    } else 0

    var scope = rememberCoroutineScope()
    scope.launch {
        val flow = locationTracker.getLocationsFlow()
        locationTracker.startTracking()
        flow.distinctUntilChanged().collect {
            lastLocation = Point(it.latitude, it.longitude)
        }
    }

    MaterialTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 250.dp,
            modifier = Modifier.fillMaxSize(),
            sheetContent = { Box(modifier = Modifier) },
        ) {
            Maps(
                modifier = Modifier.fillMaxSize(),
                newCameraPosition = newCameraPosition,
                cameraPositionUpdated = { newCameraPosition = null },
                extInsets = extInsets,
            )
            Searcher(
                ptvService = PtvService(),
                expanded = searchExpandedState,
                onExpandedChange = { searchExpandedState = it },
                text = searchTextState,
                onTextChange = { searchTextState = it },
                onRouteChange = {}
            )

            Box(
                Modifier.windowInsetsPadding(WindowInsets.safeContent.add(WindowInsets(bottom = extInsets))),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = {
                        newCameraPosition = lastLocation
                    },
                ) {
                    Icon(painterResource(Res.drawable.my_location_24), "Move to current location")
                }
            }
        }
    }
}
