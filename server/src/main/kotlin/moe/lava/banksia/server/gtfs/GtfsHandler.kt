package moe.lava.banksia.server.gtfs

import com.lightningkite.kotlinx.serialization.csv.CsvFormat
import com.lightningkite.kotlinx.serialization.csv.StringDeferringConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.util.logging.Logger
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.EmptySerializersModule
import moe.lava.banksia.model.Route
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.Shape
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.ShapeDao
import moe.lava.banksia.server.gtfs.structures.GtfsRoute
import moe.lava.banksia.server.gtfs.structures.GtfsShape
import moe.lava.banksia.util.Point
import java.io.File
import java.util.zip.ZipFile

class GtfsHandler(
    private val log: Logger,
    private val client: HttpClient,

    private val routeDao: RouteDao,
    private val shapeDao: ShapeDao,
) {
    private val csv = CsvFormat(StringDeferringConfig(EmptySerializersModule()))
    private val datasetPath = File("/tmp/banksia", "dataset.zip")

    suspend fun update(datasetUrl: String) {
        val parentDir = datasetPath.parentFile
        if (parentDir.exists() && !log.isTraceEnabled) // XXX: hacky check for dev env
            parentDir.deleteRecursively()

        parentDir.mkdirs()

        log.info("fetching..")
        client.prepareRequest {
            url(datasetUrl)
        }.execute { resp ->
            if (!datasetPath.exists())
                resp.bodyAsChannel().copyAndClose(datasetPath.writeChannel())
            log.info("fetched!")
        }

        log.info("extracting...")
        val files = extractAll(datasetPath)

        log.info("parsing routes...")
        val routes = files
            .filter { it.name == "routes.txt" }
            .flatMap { fd -> parseRoutes(fd) }

        log.info("inserting routes...")
        routeDao.deleteAll()
        routeDao.insertAll(*routes.toTypedArray())

        log.info("parsing shapes...")
        val shapes = files
            .filter { it.name == "shapes.txt" }
            .flatMap { fd -> parseShapes(fd) }

        log.info("inserting shapes...")
        shapeDao.deleteAll()
        shapeDao.insertAll(*shapes.toTypedArray())

        log.info("done!")
    }

    private fun parseRoutes(fd: File) =
        fd.parseCsv<GtfsRoute>()
            .map { with(it) {
                Route(
                    id = route_id,
                    type = RouteType.from(fd.parentFile.name.toInt()),
                    number = route_short_name,
                    name = route_long_name,
                )
            } }

    private fun parseShapes(fd: File) =
        fd.parseCsv<GtfsShape>()
            .groupBy { it.shape_id }
            .map { (id, group) ->
                val points = group
                    .sortedBy { it.shape_pt_sequence }
                    .map { Point(it.shape_pt_lat, it.shape_pt_lon) }

                Shape(id, points)
            }


    private fun extract(fd: File): List<File> {
        val outputs = mutableListOf<File>()
        ZipFile(fd).use { zip ->
            for (entry in zip.entries()) {
                zip.getInputStream(entry).use { input ->
                    val out = File(fd.parentFile, entry.name)
                    out.parentFile.mkdirs()
                    out.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    outputs.add(out)
                }
            }
        }
        return outputs
    }

    private fun extractAll(fd: File) = extract(fd).flatMap(::extract)

    private fun <T> File.parseCsv(): List<T> = this
        .readText()
        .replace("\uFEFF", "") // remove bom
        .replace("\r\n", "\n") // crlf -> lf
        .let { csv.decodeFromString(it) }
}
