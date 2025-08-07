package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import moe.lava.banksia.model.Shape

@Dao
interface ShapeDao {
    @Query("SELECT * FROM Shape WHERE id == :id")
    suspend fun get(id: String): Shape?

    @Insert
    suspend fun insertAll(vararg shapes: Shape)

    @Delete
    suspend fun delete(shape: Shape)

    @Query("DELETE FROM Shape")
    suspend fun deleteAll()
}
