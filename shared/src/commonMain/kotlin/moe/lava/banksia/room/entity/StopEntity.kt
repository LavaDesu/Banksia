package moe.lava.banksia.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import moe.lava.banksia.model.Stop
import moe.lava.banksia.util.Point

@Entity("Stop")
data class StopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    @ColumnInfo(index = true) val parent: String,
    val hasWheelChairBoarding: Boolean,
    val level: String,
    val platformCode: String,
) {
    fun asModel() = Stop(id, name, Point(lat, lng), parent, hasWheelChairBoarding, level, platformCode)
}

fun Stop.asEntity() = StopEntity(id, name, pos.lat, pos.lng, parent, hasWheelChairBoarding, level, platformCode)
