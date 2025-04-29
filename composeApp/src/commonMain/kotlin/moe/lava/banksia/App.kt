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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.api.ptv.structures.PtvRoute
import moe.lava.banksia.api.ptv.structures.getProperties
import moe.lava.banksia.native.BanksiaTheme
import moe.lava.banksia.native.maps.Maps
import moe.lava.banksia.native.maps.Point
import moe.lava.banksia.native.maps.Polyline
import moe.lava.banksia.native.maps.getScreenHeight
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.my_location_24
import moe.lava.banksia.ui.Searcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

fun buildBounds(points: List<Point>): Pair<Point, Point> {
    var north = -Double.MAX_VALUE
    var south = Double.MAX_VALUE
    var east = -Double.MAX_VALUE
    var west = Double.MAX_VALUE
    points.forEach {
        if (it.lat > north)
            north = it.lat;
        if (it.lat < south)
            south = it.lat;
        if (it.lng > east)
            east = it.lng;
        if (it.lng < west)
            west = it.lng;
    }
    return Pair(Point(north, east), Point(south, west))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val ptvService = remember { PtvService() }

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
    var newCameraPosition by remember {
        mutableStateOf<Pair<Point, Pair<Point, Point>?>?>(
            Pair(Point(-37.8136, 144.9631), null)
        )
    }
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

    var route by remember { mutableStateOf<PtvRoute?>(null) }
    val polylines = remember { mutableStateListOf<Polyline>() }

    LaunchedEffect(route) {
        val route = route
        if (route == null)
            return@LaunchedEffect
        val geoRoute = ptvService.route(route.routeId, true)
        val colour = route.routeType.getProperties().colour

        val allPoints = mutableListOf<Point>()
        polylines.clear()
        geoRoute.geopath.forEach { pp ->
            // TODO: use gtfs colours
            pp.paths.forEach { sp ->
                val polyline = sp.replace(", ", ",")
                    .split(" ")
                    .map { coord ->
                        val s = coord.split(",")
                        val point = Point(s[0].toDouble(), s[1].toDouble())
                        allPoints.add(point)
                        point
                    }
                polylines.add(Polyline(polyline, colour))
            }
        }
        val bounds = buildBounds(allPoints)
        newCameraPosition = Pair(Point(0.0, 0.0), bounds)
    }

    var sheetSwipeEnabled by remember { mutableStateOf(true) }
    var peekHeight by remember { mutableStateOf(128.dp) }
    var peekHeightMultiplier by remember { mutableFloatStateOf(1F) }

    BanksiaTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight * peekHeightMultiplier,
            modifier = Modifier.fillMaxSize(),
            sheetContent = { Box(modifier = Modifier) },
            sheetSwipeEnabled = sheetSwipeEnabled,
        ) {
            Maps(
                modifier = Modifier.fillMaxSize(),
                newCameraPosition = newCameraPosition,
                cameraPositionUpdated = { newCameraPosition = null },
                extInsets = WindowInsets(top = with(LocalDensity.current) { 56.dp.roundToPx() }, bottom = extInsets),
                polylines = polylines,
            )
            Searcher(
                ptvService = ptvService,
                expanded = searchExpandedState,
                onExpandedChange = { searchExpandedState = it },
                text = searchTextState,
                onTextChange = { searchTextState = it },
                onRouteChange = { route = it }
            )

            PredictiveBackHandler(scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) { progress ->
                sheetSwipeEnabled = false
                try {
                    progress.collect { backEvent ->
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                            peekHeightMultiplier = 1F - backEvent.progress
                        }
                    }
                    if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded)
                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    else if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded)
                        scope.launch {
                            scaffoldState.bottomSheetState.hide()
                            peekHeightMultiplier = 1F
                        }
                } catch (_: CancellationException) {
                    peekHeightMultiplier = 1F
                }
                sheetSwipeEnabled = true
            }

            Box(
                Modifier.windowInsetsPadding(WindowInsets.safeContent.add(WindowInsets(bottom = extInsets))),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = {
                        newCameraPosition = Pair(lastLocation, null)
                    },
                ) {
                    Icon(painterResource(Res.drawable.my_location_24), "Move to current location")
                }
            }
        }
    }
}
