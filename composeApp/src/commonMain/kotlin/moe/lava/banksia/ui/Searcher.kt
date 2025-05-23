package moe.lava.banksia.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.unit.dp
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.api.ptv.structures.ComposableIcon
import moe.lava.banksia.api.ptv.structures.PtvRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searcher(
    ptvService: PtvService,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onRouteChange: (PtvRoute) -> Unit,
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
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = animatedPadding),
            inputField = {
                SearchBarDefaults.InputField(
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
                                    onTextChange(route.getShortFullName())
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
