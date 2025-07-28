package moe.lava.banksia.ui.state

import moe.lava.banksia.api.ptv.structures.PtvRouteType

sealed class InfoPanelState {
    abstract val loading: Boolean

    data object None : InfoPanelState() {
        override val loading = false
    }

    data class Route(
        val name: String,
        val type: PtvRouteType,
    ) : InfoPanelState() {
        override val loading = false
    }

    data class Stop(
        val id: Int,
        val name: String,
        val subname: String? = null,
        val departures: List<Departure>? = null,
    ) : InfoPanelState() {
        override val loading: Boolean
            get() = departures == null

        data class Departure(val directionName: String, val formattedTimes: String)
    }
}