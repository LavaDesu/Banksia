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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.api.ptv.structures.PtvRoute
import moe.lava.banksia.api.ptv.structures.PtvStop
import moe.lava.banksia.api.ptv.structures.getProperties
import moe.lava.banksia.log
import moe.lava.banksia.native.maps.CameraPosition
import moe.lava.banksia.native.maps.CameraPositionBounds
import moe.lava.banksia.native.maps.Marker
import moe.lava.banksia.native.maps.MarkerType
import moe.lava.banksia.native.maps.Point
import moe.lava.banksia.native.maps.Polyline
import moe.lava.banksia.ui.state.InfoPanelState
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.util.BoxedValue
import moe.lava.banksia.util.BoxedValue.Companion.box

sealed class BanksiaEvent {}

data class BanksiaViewState(
    val routes: List<PtvRoute> = listOf(),
)

class BanksiaViewModel : ViewModel() {
    private val iState = MutableStateFlow(BanksiaViewState())
    val state = iState.asStateFlow()

    private val iInfoState = MutableStateFlow<InfoPanelState>(InfoPanelState.None)
    val infoState = iInfoState.asStateFlow()

    private val iMapState = MutableStateFlow(MapState())
    val mapState = iMapState.asStateFlow()
    private val iCameraChangeEmitter = MutableSharedFlow<BoxedValue<CameraPosition>>()
    val cameraChangeEmitter = iCameraChangeEmitter.asSharedFlow()

    private val ptvService = PtvService()
    private var locationTrackerJob: Job? = null
    private var lastKnownLocation: Point? = null

    init {
        viewModelScope.launch {
            requestRoutes()
        }
    }

    fun handleEvent(event: BanksiaEvent) {}

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

    private suspend fun requestRoutes() {
        val routes = ptvService.routes().sortedWith(
            compareBy(
                { it.gtfsSubType()?.ordinal },
                { it.routeNumber.toIntOrNull() },
                { it.routeName }
            )
        )
        iState.update { it.copy(routes = routes) }
    }

    fun switchRoute(route: PtvRoute?) {
        iMapState.update { MapState() }
        iInfoState.update {
            if (route == null)
                InfoPanelState.None
            else
                InfoPanelState.Route(
                    name = route.routeName,
                    type = route.routeType,
                )
        }

        if (route != null) {
            viewModelScope.launch { buildPolylines(route) }
            viewModelScope.launch { buildStops(route) }
//            viewModelScope.launch { buildDepartures() }
//            viewModelScope.launch { buildRuns() }
        }
    }

    // [TODO]: Cleanup
    suspend fun switchStop(stop: PtvStop?) {
        if (stop == null) {
            iInfoState.update { InfoPanelState.None }
            return
        }
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
            val direction = ptvService.cache.direction(dep.directionId, dep.routeId) ?: return@forEach
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
        val colour = routeWithGeo.routeType.getProperties().colour

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

//    private suspend fun buildDepartures(route: PtvRoute) {
//        val directions = ptvService.directionsByRoute(route.routeId)
//
//        iState.update {
//            it.copy(routeState = it.routeState?.copy(directions = directions))
//        }
//    }
//
//    private suspend fun buildRuns() {
//        val route = iState.value.routeState?.route ?: return
//
//        val directions = ptvService.directionsByRoute(route.routeId)
//
//        iState.update {
//            it.copy(routeState = it.routeState?.copy(directions = directions))
//        }
//    }

    private suspend fun buildStops(route: PtvRoute) {
        val stops = ptvService.stopsByRoute(route.routeId, route.routeType)
        val markers = mutableListOf<Marker>()
        val colour = route.routeType.getProperties().colour

        for (stop in stops) {
            if (stop.stopLatitude != null && stop.stopLongitude != null) {
                val pos = Point(stop.stopLatitude!!, stop.stopLongitude!!)

                val marker = Marker(
                    point = pos,
                    type = MarkerType.GENERIC_STOP,
                    colour = colour,
                    onClick = {
                        viewModelScope.launch { switchStop(stop) }
                        false
                    }
                )
                markers.add(marker)
            }
        }

        iMapState.update { it.copy(markers = markers) }
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
