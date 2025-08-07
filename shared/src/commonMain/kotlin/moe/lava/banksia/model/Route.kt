package moe.lava.banksia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Route(
    @PrimaryKey val id: String,
    val type: RouteType,
    val number: String?,
    val name: String,
)
