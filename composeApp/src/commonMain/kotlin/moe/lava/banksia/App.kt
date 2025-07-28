package moe.lava.banksia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import kotlinx.coroutines.launch
import moe.lava.banksia.native.BanksiaTheme
import moe.lava.banksia.native.maps.Maps
import moe.lava.banksia.native.maps.Point
import moe.lava.banksia.native.maps.getScreenHeight
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.my_location_24
import moe.lava.banksia.ui.BanksiaViewModel
import moe.lava.banksia.ui.InfoPanel
import moe.lava.banksia.ui.Searcher
import moe.lava.banksia.ui.state.InfoPanelState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

val MELBOURNE = Point(-37.8136, 144.9631)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(
    viewModel: BanksiaViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    val locationFactory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember { locationFactory.createLocationTracker() }
    BindLocationTrackerEffect(locationTracker)
    viewModel.bindTracker(locationTracker)
    scope.launch { locationTracker.startTracking() }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val infoState by viewModel.infoState.collectAsStateWithLifecycle()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val sheetState = scaffoldState.bottomSheetState
    val extInsets = if (
        sheetState.currentValue != SheetValue.Hidden ||
        sheetState.targetValue != SheetValue.Hidden
    ) {
        val offset = runCatching { sheetState.requireOffset() }
        val scaffoldOffset = offset.getOrDefault(0.0f).roundToInt()
        (getScreenHeight() - scaffoldOffset - WindowInsets.safeDrawing.getBottom(LocalDensity.current)).coerceAtLeast(0)
    } else 0

    LaunchedEffect(infoState) {
        if (infoState !is InfoPanelState.None)
            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
        else
            scope.launch { scaffoldState.bottomSheetState.hide() }
    }

    var searchTextState by rememberSaveable { mutableStateOf("") }
    var searchExpandedState by rememberSaveable { mutableStateOf(false) }
    var sheetSwipeEnabled by rememberSaveable { mutableStateOf(true) }
    var handleHeight by remember { mutableStateOf(0.dp) }
    var peekHeight by remember { mutableStateOf(0.dp) }
    var peekHeightMultiplier by remember { mutableFloatStateOf(1F) }

    BanksiaTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = (handleHeight + peekHeight) * peekHeightMultiplier,
            modifier = Modifier.fillMaxSize(),
            sheetContent = {
                InfoPanel(
                    state = infoState,
                    onEvent = viewModel::handleEvent,
                    onPeekHeightChange = { peekHeight = it },
                )
            },
            sheetDragHandle = {
                val density = LocalDensity.current
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .onSizeChanged {
                            handleHeight = with(density) { it.height.toDp() }
                        }
                ) {
                    BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))
                }
            },
            sheetSwipeEnabled = sheetSwipeEnabled,
        ) {
            Maps(
                modifier = Modifier.fillMaxSize(),
                cameraPositionFlow = viewModel.cameraChangeEmitter,
                extInsets = WindowInsets(top = with(LocalDensity.current) {
                    SearchBarDefaults.InputFieldHeight.roundToPx()
                }, bottom = extInsets),
                markers = state.markers,
                setLastKnownLocation = viewModel::setLastKnownLocation,
                polylines = state.polylines,
            )
            Searcher(
                routes = state.routes,
                expanded = searchExpandedState,
                onExpandedChange = {
                    searchExpandedState = it
                    if (it)
                        scope.launch { scaffoldState.bottomSheetState.hide() }
                },
                text = searchTextState,
                onTextChange = { searchTextState = it },
                onRouteChange = { viewModel.switchRoute(it) }
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
                    onClick = { viewModel.centreCameraToLocation() },
                ) {
                    Icon(painterResource(Res.drawable.my_location_24), "Move to current location")
                }
            }
        }
    }
}
