package moe.lava.banksia.room

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import moe.lava.banksia.model.Route
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.Shape
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.ShapeDao
import androidx.room.Database as DatabaseAnnotation

@DatabaseAnnotation(entities = [Route::class, Shape::class], version = 1)
@TypeConverters(RouteType.Companion::class)
abstract class Database : RoomDatabase() {
    abstract fun getRouteDao(): RouteDao
    abstract fun getShapeDao(): ShapeDao

    companion object {
        fun build(base: Builder<Database>) =
            base.fallbackToDestructiveMigrationOnDowngrade(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}
