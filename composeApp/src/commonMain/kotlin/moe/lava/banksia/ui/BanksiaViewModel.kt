package moe.lava.banksia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.geo.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import moe.lava.banksia.ui.BoxedValue.Companion.box

data class RouteState(
    val route: PtvRoute,
    val stops: List<PtvStop>? = null,
)

data class StopState(
    val stop: PtvStop,
//    val departures: List<PtvDeparture>? = null,
    val departures: List<Pair<String, String>>? = null,
)

data class BanksiaViewState(
    val routeState: RouteState? = null,
    val stopState: StopState? = null,

    val routes: List<PtvRoute> = listOf(),

    val markers: List<Marker> = listOf(),
    val polylines: List<Polyline> = listOf(),
)

class BoxedValue<T>(val value: T) {
    operator fun component1() = value

    companion object {
        fun <T> T.box() = BoxedValue(this)
    }
}

class BanksiaViewModel : ViewModel() {
    private val iState = MutableStateFlow(BanksiaViewState())
    val state: StateFlow<BanksiaViewState> = iState.asStateFlow()

    private val ptvService = PtvService()
    private var locationTrackerJob: Job? = null
    private var lastKnownLocation: Point? = null

    private val iCameraChangeEmitter = MutableSharedFlow<BoxedValue<CameraPosition>>()
    val cameraChangeEmitter = iCameraChangeEmitter.asSharedFlow()

    init {
        viewModelScope.launch {
            requestRoutes()
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

    fun switchRoute(newRoute: PtvRoute?) {
        val routeState = newRoute?.let { RouteState(it) }
        if (iState.value.routeState == routeState)
            return

        iState.update {
            it.copy(
                routeState = routeState,
                markers = listOf(),
                polylines = listOf(),
            )
        }

        if (routeState != null) {
            viewModelScope.launch {
                async { buildPolylines() }
                async { buildMarkers() }
            }
        }
    }

    // [TODO]: Cleanup
    suspend fun switchStop(stop: PtvStop?) {
        iState.update { state ->
            state.copy(stopState = stop?.let { StopState(it) })
        }

        if (stop == null)
            return

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
                Pair(name, "No departures")
            else
                Pair(name, list.joinToString(" | "))
        }
        iState.update {
            it.copy(stopState = it.stopState?.copy(departures = departures))
        }
    }

    private suspend fun buildPolylines() {
        val route = iState.value.routeState?.route ?: return

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

        iState.update { it.copy(polylines = polylines) }
        newCameraPosition?.let { iCameraChangeEmitter.emit(it.box()) }
    }

    private suspend fun buildMarkers() {
        val route = iState.value.routeState?.route ?: return

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

        iState.update {
            it.copy(
                routeState = it.routeState?.copy(stops = stops),
                markers = markers
            )
        }
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
