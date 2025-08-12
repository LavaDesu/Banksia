package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.RouteEntity
import moe.lava.banksia.room.entity.StopEntity

@Dao
interface RouteDao {
    @Query("SELECT * FROM Route")
    suspend fun getAll(): List<RouteEntity>

    @Query("SELECT * FROM Route WHERE id == :id")
    suspend fun get(id: String): RouteEntity?

    @Insert
    suspend fun insertAll(vararg routes: RouteEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg routes: RouteEntity)

    @Delete
    suspend fun delete(route: RouteEntity)

    @Query("DELETE FROM Route")
    suspend fun deleteAll()

    @Query("""
        SELECT Stop.* FROM Stop
        INNER JOIN StopTime ON StopTime.stopId == Stop.id
        INNER JOIN Trip ON Trip.id == StopTime.tripId
        WHERE Trip.routeId == :id
        GROUP BY Stop.id
    """)
    suspend fun stops(id: String): List<StopEntity>

    @Query("""
        SELECT Stop.* FROM Stop
        INNER JOIN Stop Child ON Child.parent == Stop.id
        INNER JOIN StopTime ON StopTime.stopId == Child.id
        INNER JOIN Trip ON Trip.id == StopTime.tripId
        WHERE Trip.routeId == :id
        GROUP BY Stop.id
    """)
    suspend fun stopsParent(id: String): List<StopEntity>
}
