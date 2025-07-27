package moe.lava.banksia.api.ptv

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.lava.banksia.Constants
import moe.lava.banksia.api.ptv.structures.PtvDeparture
import moe.lava.banksia.api.ptv.structures.PtvDirection
import moe.lava.banksia.api.ptv.structures.PtvRoute
import moe.lava.banksia.api.ptv.structures.PtvRouteType
import moe.lava.banksia.api.ptv.structures.PtvStop
import moe.lava.banksia.log
import okio.ByteString.Companion.encodeUtf8
import kotlin.random.Random

object Responses {
    @Serializable
    data class PtvRouteResponse(val route: PtvRoute)
    @Serializable
    data class PtvRoutesResponse(val routes: List<PtvRoute>)

    @Serializable
    data class PtvStopsResponse(val stops: List<PtvStop>)

    @Serializable
    data class PtvDeparturesResponse(val departures: List<PtvDeparture>, val routes: Map<String, PtvRoute>, val directions: Map<String, PtvDirection>)

    @Serializable
    data class PtvDirectionsResponse(val directions: List<PtvDirection>)
}


class PtvService {
    class PtvCache(
        private val service: PtvService,
        private val directions: HashMap<Pair<Int, Int>, PtvDirection> = HashMap(),
    ) {
        suspend fun direction(directionID: Int, routeID: Int): PtvDirection? {
            val ret = directions[Pair(directionID, routeID)]
            if (ret == null) {
                val res = service.directionsByRoute(routeID)
                for (dir in res)
                    directions[Pair(dir.directionId, dir.routeId)] = dir
            }

            return ret ?: directions[Pair(directionID, routeID)]
        }
    }

    val cache = PtvCache(this)

    private val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url("https://timetableapi.ptv.vic.gov.au/v3/")
        }
    }

    constructor() {
        client.plugin(HttpSend).intercept { req ->
            req.parameter("devid", Constants.devid)
            @OptIn(ExperimentalStdlibApi::class)
            req.parameter("nonce", Random.nextBytes(6).toHexString())
            val fullPath = req.url.build().encodedPathAndQuery
            val hash = fullPath.encodeUtf8().hmacSha1(Constants.key.encodeUtf8()).hex()
            req.parameter("signature", hash)
            log("ktor.intercept", req.url.build().encodedPathAndQuery)
            execute(req)
        }
    }

    suspend fun route(id: Int, includeGeopath: Boolean = false): PtvRoute {
        val response: Responses.PtvRouteResponse = client.get("routes") {
            url {
                appendPathSegments(id.toString())
                parameters.append("include_geopath", if (includeGeopath) "true" else "false")
            }
        }.body()
        return response.route
    }

    suspend fun routes(): List<PtvRoute> {
        val response: Responses.PtvRoutesResponse = client.get("routes").body()
        return response.routes
    }

    suspend fun stopsByRoute(routeId: Int, routeType: PtvRouteType): List<PtvStop> {
        val response: Responses.PtvStopsResponse = client.get("stops") {
            url {
                appendPathSegments(
                    "route",
                    routeId.toString(),
                    "route_type",
                    routeType.ordinal.toString()
                )
            }
        }.body()
        return response.stops
    }

    suspend fun directionsByRoute(routeId: Int): List<PtvDirection> {
        val response: Responses.PtvDirectionsResponse = client.get("directions") {
            url {
                appendPathSegments("route", routeId.toString())
            }
        }.body()
        return response.directions
    }

    suspend fun direction(id: Int, routeType: PtvRouteType?): List<PtvDirection> {
        val response: Responses.PtvDirectionsResponse = client.get("directions") {
            url {
                appendPathSegments(id.toString())
                if (routeType != null)
                    appendPathSegments("route_type", routeType.ordinal.toString())
            }
        }.body()
        return response.directions
    }

    suspend fun departures(routeType: PtvRouteType, stopId: Int): Responses.PtvDeparturesResponse =
        client.get("departures") {
            url {
                appendPathSegments(
                    "route_type",
                    routeType.ordinal.toString(),
                    "stop",
                    stopId.toString()
                )
                parameter("expand", "Route")
                parameter("expand", "Direction")
            }
        }.body()
}
