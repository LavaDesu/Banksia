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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.lava.banksia.api.ptv.structures.ComposableRouteIcon
import moe.lava.banksia.ui.state.SearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searcher(
    state: SearchState,
    onEvent: (BanksiaEvent) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
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
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = animatedPadding),
            shadowElevation = 6.dp, // Elevation level 3
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier.padding(horizontal = 20.dp - animatedPadding),
                    query = state.text,
                    onQueryChange = { onEvent(BanksiaEvent.SearchUpdate(it)) },
                    onSearch = {},
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (expanded && state.text.isNotEmpty())
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.clickable { onEvent(BanksiaEvent.SearchUpdate("")) }
                            )
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                for (entry in state.entries) {
                    item {
                        ListItem(
                            headlineContent = { Text(entry.mainText) },
                            supportingContent = { entry.subText?.let { Text(it) } },
                            leadingContent = { ComposableRouteIcon(routeType = entry.routeType) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    onExpandedChange(false)
                                    onEvent(BanksiaEvent.SearchUpdate(""))
                                    onEvent(BanksiaEvent.SelectRoute(entry.routeId))
                                }
                        )
                    }
                }
            }
        }
    }
}
