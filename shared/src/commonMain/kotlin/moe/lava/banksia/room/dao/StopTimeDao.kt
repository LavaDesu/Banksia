package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.StopTimeEntity

@Dao
interface StopTimeDao {
    @Query("SELECT * FROM StopTime")
    suspend fun getAll(): List<StopTimeEntity>

    @Query("SELECT * FROM StopTime WHERE tripId == :tripId")
    suspend fun get(tripId: String): StopTimeEntity?

    @Query("SELECT * FROM StopTime WHERE tripId IN (:tripIds)")
    suspend fun get(tripIds: List<String>): List<StopTimeEntity>

    @Insert
    suspend fun insertAll(vararg stopTimes: StopTimeEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg stopTimes: StopTimeEntity)

    @Delete
    suspend fun delete(stopTime: StopTimeEntity)

    @Query("DELETE FROM StopTime")
    suspend fun deleteAll()
}
