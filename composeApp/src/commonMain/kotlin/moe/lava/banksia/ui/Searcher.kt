package moe.lava.banksia.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.api.ptv.structures.ComposableIcon
import moe.lava.banksia.api.ptv.structures.PtvRoute
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Searcher(
    ptvService: PtvService,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    route: PtvRoute?,
    text: String,
    onTextChange: (String) -> Unit,
    onRouteChange: (PtvRoute?) -> Unit,
) {
    val animatedPadding by animateDpAsState(
        if (expanded) {
            0.dp
        } else {
            20.dp
        },
        label = "padding"
    )
    var routes by remember { mutableStateOf(listOf<PtvRoute>()) }
    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(Unit) {
            val localRoutes = ptvService.routes()
            routes = localRoutes.sortedWith(
                compareBy(
                    { it.gtfsSubType()?.ordinal },
                    { it.routeNumber.toIntOrNull() },
                    { it.routeName }
                )
            )
        }
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = animatedPadding),
            shadowElevation = 6.dp, // Elevation level 3
            inputField = {
                var backProgress by remember { mutableFloatStateOf(1f) }
                var backEdgeIsLeft by remember { mutableStateOf<Boolean?>(null) }
                val boxOpacityState by animateFloatAsState((1f - backProgress).pow(3))
                val slideState by animateDpAsState((50 * backProgress).dp)
                val slidePadding = if (backEdgeIsLeft == true)
                    PaddingValues(start = slideState)
                else if (backEdgeIsLeft == false)
                    PaddingValues(end = slideState)
                else
                    PaddingValues()

                PredictiveBackHandler(enabled = route != null) { progress ->
                    try {
                        progress.collect { backEvent ->
                            backProgress = backEvent.progress
                            backEdgeIsLeft = backEvent.swipeEdge == 0
                        }
                        backProgress = 1f
                        onRouteChange(null)
                    } catch (_: CancellationException) {
                        backProgress = 0f
                    }
                    backEdgeIsLeft = null
                }
                SearchBarDefaults.InputField(
                    enabled = route == null,
                    modifier = Modifier
                        .alpha(1f - boxOpacityState)
                        .padding(horizontal = 20.dp - animatedPadding),
                    query = text,
                    onQueryChange = onTextChange,
                    onSearch = {},
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (expanded && text.isNotEmpty())
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.clickable { onTextChange("") }
                            )
                    }
                )
                LaunchedEffect(route) {
                    backProgress = if (route != null) 0f else 1f;
                }
                if (route != null) {
                    Box(
                        modifier = Modifier
                            .alpha(boxOpacityState)
                            .sizeIn(
                                minHeight = SearchBarDefaults.InputFieldHeight,
                                maxHeight = SearchBarDefaults.InputFieldHeight,
                            )
                            .fillMaxWidth()
                            .padding(slidePadding)
                    ) {
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = {
                                onRouteChange(null)
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Clear route")
                        }
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 50.dp)
                                .align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            route.routeType.ComposableIcon()
                            Spacer(Modifier.width(15.dp))
                            Text(
                                route.routeNumber.ifEmpty { route.routeName },
                                style = MaterialTheme.typography.headlineSmall,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    }
                }
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                for (route in routes) {
                    if (!route.routeNumber.contains(text) &&
                        !route.routeName.lowercase().contains(text.lowercase()))
                        continue
                    item {
                        ListItem(
                            headlineContent = { Text(route.routeNumber.ifEmpty { route.routeName }) },
                            supportingContent = {
                                if (route.routeNumber.isNotEmpty()) {
                                    Text(route.routeName)
                                }
                            },
                            leadingContent = { route.routeType.ComposableIcon() },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    onTextChange("")
                                    onExpandedChange(false)
                                    onRouteChange(route)
                                }
                        )
                    }
                }
            }
        }
    }
}
