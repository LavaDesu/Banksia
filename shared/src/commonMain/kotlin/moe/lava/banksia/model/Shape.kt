package moe.lava.banksia.model

import kotlinx.serialization.Serializable
import moe.lava.banksia.util.Point

typealias ShapePath = List<Point>

@Serializable
data class Shape(
    val id: String,
    val path: ShapePath,
)
