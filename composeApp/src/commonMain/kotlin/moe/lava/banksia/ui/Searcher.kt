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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searcher(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
) {
    val animatedPadding by animateDpAsState(
        if (expanded) {
            0.dp
        } else {
            20.dp
        },
        label = "padding"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(Unit) {
            /*cache.routes()*/
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
                /*val r = cache.sortedRoutes()
                for ((_, route) in r) {
                    if (!route.route_number.contains(text) &&
                        !route.route_name.lowercase().contains(text.lowercase()))
                        continue
                    item {
                        ListItem(
                            headlineContent = { Text(route.route_number.ifEmpty { route.route_name }) },
                            supportingContent = {
                                if (route.route_number.isNotEmpty()) {
                                    Text(route.route_name)
                                }
                            },
                            leadingContent = { route.route_type.ComposableIcon() },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    text = "${route.route_number} - ${route.route_name}"

                                    onRouteChanged(route)
                                    expanded = false
                                }
                        )
                    }
                }*/
            }
        }
    }

}