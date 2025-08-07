package moe.lava.banksia.model

import androidx.room.TypeConverter

enum class RouteType(val value: Int) {
    MetroTrain(2),
    MetroTram(3),
    MetroBus(4),
    RegionalTrain(1),
    RegionalCoach(5),
    RegionalBus(6),
    SkyBus(11),
    Interstate(10),
    ;

    companion object {
        @TypeConverter
        fun from(value: Int) = RouteType.entries.first { it.value == value }

        @TypeConverter
        fun to(routeType: RouteType) = routeType.value
    }
}
