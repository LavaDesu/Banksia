package moe.lava.banksia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.api.ptv.structures.PtvStop

@Composable
fun StopInfoPanel(
    ptvService: PtvService,
    stop: PtvStop,
    onPeekHeightChange: (Dp) -> Unit,
) {
    var departures by remember { mutableStateOf<List<Pair<String, String>>>(listOf()) }
    var loading by remember { mutableStateOf(true) }
    // [TODO]: Cleanup
    LaunchedEffect(stop) {
        loading = true
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

            val date = Instant.parse(dep.estimatedDepartureUtc ?: dep.scheduledDepartureUtc)
            val min = (date - Clock.System.now()).inWholeMinutes
            if (min <= -5)
                return@forEach
            if (min >= 65)
                element.add("${((min + 30.0) / 60.0).toInt()}hr")
            else
                element.add("${min}mn")
        }
        departures = timetable.values.sortedBy { it.first }.map { (name, list) ->
            if (list.isEmpty())
                Pair(name, "No departures")
            else
                Pair(name, list.joinToString(" | "))
        }
        loading = false
    }
    val localDensity = LocalDensity.current;
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(max = 250.dp)
            .verticalScroll(rememberScrollState())
            .onSizeChanged {
                onPeekHeightChange(with(localDensity) { it.height.toDp().coerceAtMost(250.dp) })
            }
    ) {
        Box {
            Column(Modifier.fillMaxWidth()) {
                val split = stop.stopName.split("/")
                val mainName = split[0]
                val subName = split.getOrNull(1)
                Text(
                    mainName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                if (subName != null)
                    Text(
                        "/ $subName",
                        modifier = Modifier.padding(start = 5.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )
                if (!loading)
                {
                    Spacer(Modifier.height(5.dp))
                    departures.forEach { (name, formatted) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(formatted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 5.dp))
                        }
                    }
                }
            }
            if (loading)
                CircularProgressIndicator(
                    modifier = Modifier.width(32.dp).align(Alignment.CenterEnd)
                )
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeContent))
    }
}
