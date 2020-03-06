package com.example.wallet.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wallet.data.entity.DLocation


@Dao
interface LocationDAO {

    @Query("SELECT * FROM location ORDER BY date DESC")
    fun getLocations(): List<DLocation>

    @Query("SELECT * FROM location WHERE status = 'HOMEWRK' ORDER BY date DESC LIMIT 2")
    fun getHomewrk(): List<DLocation>

    @Query("SELECT * FROM location WHERE status in ('LOCVER', 'NOTIFIED') ORDER BY date DESC LIMIT 1")
    fun getPrevLoc(): List<DLocation>

    @Query("UPDATE location SET status = :stats WHERE id = :id ")
    fun updateHomewrk(id: Long, stats: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(location: DLocation)

    @Query("DELETE FROM location")
    suspend fun deleteAllLocation()

}