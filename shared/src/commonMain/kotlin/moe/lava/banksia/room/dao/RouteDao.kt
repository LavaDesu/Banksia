package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import moe.lava.banksia.model.Route

@Dao
interface RouteDao {
    @Query("SELECT * FROM Route")
    suspend fun getAll(): List<Route>

    @Query("SELECT * FROM Route WHERE id == :id")
    suspend fun get(id: String): Route?

    @Insert
    suspend fun insertAll(vararg routes: Route)

    @Delete
    suspend fun delete(route: Route)

    @Query("DELETE FROM Route")
    suspend fun deleteAll()
}
