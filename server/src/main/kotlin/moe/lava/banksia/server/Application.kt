package moe.lava.banksia.server

import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.lava.banksia.di.CommonModules
import moe.lava.banksia.server.di.ServerModules
import moe.lava.banksia.server.gtfs.GtfsHandler
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(module { single { log } })
        modules(CommonModules, ServerModules)
    }

    val client = HttpClient()

    routing {
        get("/update") {
            val datasetUrl = call.parameters["url"] ?: "https://opendata.transport.vic.gov.au/dataset/3f4e292e-7f8a-4ffe-831f-1953be0fe448/resource/e4966d78-dc64-4a1d-a751-2470c9eaf034/download/gtfs.zip"
            call.respondText("received")
            launch(context = Dispatchers.IO) {
                val handler by inject<GtfsHandler>()
                handler.update(datasetUrl)
            }
        }
    }
}
