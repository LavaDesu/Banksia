package moe.lava.banksia.ui.state

import moe.lava.banksia.data.ptv.structures.PtvRouteType

data class SearchState(
    val entries: List<SearchEntry> = listOf(),
    val text: String = "",
) {
    data class SearchEntry(
        val mainText: String,
        val subText: String?,
        val routeId: Int,
        val routeType: PtvRouteType,
    )
}
