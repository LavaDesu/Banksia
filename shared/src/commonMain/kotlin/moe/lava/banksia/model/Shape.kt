package moe.lava.banksia.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import moe.lava.banksia.room.converter.ShapeConverter
import moe.lava.banksia.util.Point

typealias ShapePath = List<Point>

@Entity
@TypeConverters(ShapeConverter::class)
data class Shape(
    @PrimaryKey val id: String,
    val path: ShapePath,
)
