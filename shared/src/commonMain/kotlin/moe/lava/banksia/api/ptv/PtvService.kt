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
import moe.lava.banksia.api.ptv.structures.Route
import moe.lava.banksia.log
import okio.ByteString.Companion.encodeUtf8

object Responses {
    @Serializable
    data class Route(val route: moe.lava.banksia.api.ptv.structures.Route)
    @Serializable
    data class Routes(val routes: List<moe.lava.banksia.api.ptv.structures.Route>)
}

class PtvService {
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
            val fullPath = req.url.build().encodedPathAndQuery
            val hash = fullPath.encodeUtf8().hmacSha1(Constants.key.encodeUtf8()).hex()
            req.parameter("signature", hash)
            log("ktor.intercept", req.url.build().encodedPathAndQuery)
            execute(req)
        }
    }

    suspend fun route(id: Int, includeGeopath: Boolean = false): Route {
        val response: Responses.Route = client.get("routes") {
            url {
                appendPathSegments(id.toString())
                parameters.append("include_geopath", if (includeGeopath) "true" else "false")
            }
        }.body()
        return response.route
    }

    suspend fun routes(): List<Route> {
        val response: Responses.Routes = client.get("routes").body()
        return response.routes
    }
}
