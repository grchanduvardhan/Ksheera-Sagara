package com.ksheera.sagara.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ksheera.sagara.data.entity.Cow

@Dao
interface CowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cow: Cow): Long

    @Update
    suspend fun update(cow: Cow)

    @Delete
    suspend fun delete(cow: Cow)

    @Query("SELECT * FROM cows WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCows(): LiveData<List<Cow>>

    @Query("SELECT * FROM cows ORDER BY name ASC")
    fun getAllCows(): LiveData<List<Cow>>

    @Query("SELECT * FROM cows WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveCowsSync(): List<Cow>

    @Query("SELECT * FROM cows WHERE id = :id")
    suspend fun getCowById(id: Long): Cow?
}
