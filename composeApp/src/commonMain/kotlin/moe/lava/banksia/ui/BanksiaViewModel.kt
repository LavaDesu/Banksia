package moe.lava.banksia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.geo.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.lava.banksia.data.ptv.PtvService
import moe.lava.banksia.data.ptv.structures.PtvRoute
import moe.lava.banksia.data.ptv.structures.PtvRouteType
import moe.lava.banksia.ui.components.getUIProperties
import moe.lava.banksia.ui.platform.maps.CameraPosition
import moe.lava.banksia.ui.platform.maps.CameraPositionBounds
import moe.lava.banksia.ui.platform.maps.Marker
import moe.lava.banksia.ui.platform.maps.Polyline
import moe.lava.banksia.ui.state.InfoPanelState
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.ui.state.SearchState
import moe.lava.banksia.util.BoxedValue
import moe.lava.banksia.util.BoxedValue.Companion.box
import moe.lava.banksia.util.LoopFlow.Companion.waitUntilSubscribed
import moe.lava.banksia.util.Point
import moe.lava.banksia.util.log
import kotlin.time.Clock
import kotlin.time.Instant

sealed class BanksiaEvent {
    data object DismissState : BanksiaEvent()

    data class SelectRoute(val id: Int?) : BanksiaEvent()
    data class SelectRun(val ref: String?) : BanksiaEvent()
    data class SelectStop(val typeAndId: Pair<PtvRouteType, Int>) : BanksiaEvent()

    data class SearchUpdate(val text: String) : BanksiaEvent()
}

data class InternalState(
    val route: Int? = null,
    val stop: Pair<PtvRouteType, Int>? = null,
    val run: String? = null,
)

class BanksiaViewModel : ViewModel() {
    private var state = InternalState()
        set(value) {
            val last = field
            field = value
            if (value.route != last.route)
                viewModelScope.launch { switchRoute(value.route) }
            if (value.stop != last.stop)
                viewModelScope.launch { switchStop(value.stop) }
            if (value.run != last.run)
                switchRun(value.run)
        }

    private val iInfoState = MutableStateFlow<InfoPanelState>(InfoPanelState.None)
    val infoState = iInfoState.asStateFlow()

    private val iMapState = MutableStateFlow(MapState())
    val mapState = iMapState.asStateFlow()
    private val iCameraChangeEmitter = MutableSharedFlow<BoxedValue<CameraPosition>>()
    val cameraChangeEmitter = iCameraChangeEmitter.asSharedFlow()

    private val iSearchState = MutableStateFlow(SearchState())
    val searchState = iSearchState.asStateFlow()

    private val ptvService = PtvService(viewModelScope)
    private var locationTrackerJob: Job? = null
    private var lastKnownLocation: Point? = null

    init {
        viewModelScope.launch { searchUpdate("") }
    }

    fun handleEvent(event: BanksiaEvent) {
        viewModelScope.launch {
            when (event) {
                is BanksiaEvent.DismissState -> dismissState()
                is BanksiaEvent.SelectRoute -> state = InternalState(route = event.id)
                is BanksiaEvent.SelectRun -> state = state.copy(run = event.ref, stop = null)
                is BanksiaEvent.SelectStop -> state = state.copy(stop = event.typeAndId, run = null)
                is BanksiaEvent.SearchUpdate -> searchUpdate(event.text)
            }
        }
    }

    fun bindTracker(locationTracker: LocationTracker) {
        locationTrackerJob = locationTracker.getLocationsFlow()
            .onEach { lastKnownLocation = Point(it.latitude, it.longitude) }
            .launchIn(viewModelScope)
    }

    fun centreCameraToLocation() {
        lastKnownLocation?.let { location ->
            viewModelScope.launch {
                log("bvm", "emitting $location")
                iCameraChangeEmitter.emit(CameraPosition(location).box())
            }
        }
    }

    fun setLastKnownLocation(location: Point) {
        lastKnownLocation = location
    }

    private fun dismissState() {
        state = InternalState()
        viewModelScope.launch { searchUpdate("") }
    }

    private suspend fun searchUpdate(text: String) {
        val entries = ptvService.routes()
            .sortedWith(
                compareBy(
                    { it.gtfsSubType()?.ordinal },
                    { it.routeNumber.toIntOrNull() },
                    { it.routeName }
                )
            )
            .filter { it.routeNumber.contains(text) || it.routeName.lowercase().contains(text.lowercase()) }
            .map { route ->
                val (main, sub) = if (route.routeNumber.isNotEmpty()) {
                    route.routeNumber to route.routeName
                } else {
                    route.routeName to null
                }

                SearchState.SearchEntry(main, sub, route.routeId, route.routeType)
            }

        iSearchState.update { SearchState(entries, text) }
    }

    private suspend fun switchRoute(routeId: Int?) {
        iMapState.update { MapState() }
        if (routeId == null) {
            iInfoState.update { InfoPanelState.None }
            return
        }

        val route = ptvService.route(routeId)
        iInfoState.update {
            InfoPanelState.Route(
                name = route.routeName,
                type = route.routeType,
            )
        }

        viewModelScope.launch { buildPolylines(route) }
        viewModelScope.launch { buildStops(route) }
        buildRuns(route)
    }

    private fun switchRun(ref: String?) {
        if (ref == null) {
            iInfoState.update { InfoPanelState.None }
            return
        }

        val lastState = state.run
        var routeName: String? = null
        ptvService.runFlow(ref, firstWithCache = true)
            .waitUntilSubscribed(iInfoState)
            .takeWhile { lastState == state.run }
            .onEach { run ->
                if (routeName == null) {
                    iInfoState.update {
                        InfoPanelState.Run(
                            direction = run.destinationName,
                            type = run.routeType,
                        )
                    }
                    routeName = ptvService.route(run.routeId).routeName
                }

                iInfoState.update {
                    InfoPanelState.Run(
                        direction = run.destinationName,
                        type = run.routeType,
                        routeName = routeName,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // [TODO]: Cleanup
    private suspend fun switchStop(typeAndId: Pair<PtvRouteType, Int>?) {
        if (typeAndId == null) {
            iInfoState.update { InfoPanelState.None }
            return
        }
        val (routeType, stopId) = typeAndId
        val stop = ptvService.stop(routeType, stopId)
        val split = stop.stopName.split("/")
        val name = split[0]
        val subname = split.getOrNull(1)
        iInfoState.update {
            InfoPanelState.Stop(
                id = stop.stopId,
                name = name,
                subname = subname,
            )
        }

        val res = ptvService.departures(stop.routeType, stop.stopId)
        // Map<
        //     Pair<DirectionId, RouteId>,
        //     Pair<DirectionName, List<DepartureTimes>>
        // >
        val timetable = HashMap<Pair<Int, Int>, Pair<String, MutableList<String>>>()
        res.departures.forEach { dep ->
            val key = Pair(dep.directionId, dep.routeId)
            val direction = ptvService.direction(dep.directionId, dep.routeId)
            val route = res.routes[dep.routeId.toString()]
            val prefix = route?.let { if (it.routeNumber == "") "" else "${it.routeNumber} - " } ?: ""
            val element = timetable.getOrPut(key) { Pair(prefix + direction.directionName, mutableListOf()) }.second
            if (element.size >= 5)
                return@forEach

            val date = Instant.Companion.parse(dep.estimatedDepartureUtc ?: dep.scheduledDepartureUtc)
            val min = (date - Clock.System.now()).inWholeMinutes
            if (min <= -5)
                return@forEach
            if (min >= 65)
                element.add("${((min + 30.0) / 60.0).toInt()}hr")
            else
                element.add("${min}mn")
        }
        val departures = timetable.values.sortedBy { it.first }.map { (name, list) ->
            if (list.isEmpty())
                InfoPanelState.Stop.Departure(name, "No departures")
            else
                InfoPanelState.Stop.Departure(name, list.joinToString(" | "))
        }
        iInfoState.update {
            if (it !is InfoPanelState.Stop)
                it
            else
                it.copy(departures = departures)
        }
    }

    private suspend fun buildPolylines(route: PtvRoute) {
        val routeWithGeo = if (route.geopath.isEmpty())
            ptvService.route(route.routeId, true)
        else
            route
        val colour = routeWithGeo.routeType.getUIProperties().colour

        val polylines = mutableListOf<Polyline>()
        val allPoints = mutableListOf<Point>()
        routeWithGeo.geopath.forEach { pp ->
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
        val newCameraPosition = if (allPoints.isNotEmpty())
            CameraPosition(bounds = buildBounds(allPoints))
        else
            null

        iMapState.update { it.copy(polylines = polylines) }
        newCameraPosition?.let { iCameraChangeEmitter.emit(it.box()) }
    }

    private fun buildRuns(route: PtvRoute) {
        ptvService
            .runsFlow(route.routeId)
            .waitUntilSubscribed(iInfoState)
            .takeWhile { state.route == route.routeId }
            .onEach { runs ->
                val markers = runs
                    .filter { it.vehiclePosition != null }
                    .map { it to it.vehiclePosition!! }
                    .distinctBy { (_, pos) -> pos.latitude to pos.longitude }
                    .map { (run, pos) ->
                        Marker.Vehicle(
                            Point(pos.latitude, pos.longitude),
                            ref = run.runRef,
                            type = route.routeType,
                        )
                    }

                iMapState.update { it.copy(vehicles = markers) }
            }
            .launchIn(viewModelScope)

    }

    private suspend fun buildStops(route: PtvRoute) {
        val stops = ptvService.stopsByRoute(route.routeId, route.routeType)
        val colour = route.routeType.getUIProperties().colour

        val markers = stops
            .filter { it.stopLatitude != null && it.stopLongitude != null }
            .map { stop ->
                Marker.Stop(
                    point = Point(stop.stopLatitude!!, stop.stopLongitude!!),
                    id = stop.stopId,
                    colour = colour,
                    type = route.routeType,
                )
            }

        iMapState.update { it.copy(stops = markers) }
    }

    private fun buildBounds(points: List<Point>): CameraPositionBounds {
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
        return CameraPositionBounds(Point(north, east), Point(south, west))
    }
}
