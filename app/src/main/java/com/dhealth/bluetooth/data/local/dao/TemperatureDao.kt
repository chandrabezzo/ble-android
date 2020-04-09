package com.dhealth.bluetooth.data.local.dao

import androidx.annotation.WorkerThread
import androidx.paging.DataSource
import androidx.room.*
import com.dhealth.bluetooth.data.constant.AppConstants
import com.dhealth.bluetooth.data.model.Temperature
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureDao {
    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.TEMPERATURE}")
    fun getAll(): DataSource.Factory<Int, Temperature>

    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.TEMPERATURE} WHERE id=:id")
    fun get(id: Long): Flow<Temperature>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(temperature: Temperature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(temperatures: MutableList<Temperature>)

    @Query("DELETE FROM ${AppConstants.TEMPERATURE}")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(temperature: Temperature)
}