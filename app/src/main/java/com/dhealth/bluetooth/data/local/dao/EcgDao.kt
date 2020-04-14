package com.dhealth.bluetooth.data.local.dao

import androidx.annotation.WorkerThread
import androidx.paging.DataSource
import androidx.room.*
import com.dhealth.bluetooth.data.constant.AppConstants
import com.dhealth.bluetooth.data.model.Ecg
import kotlinx.coroutines.flow.Flow

@Dao
interface EcgDao {
    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.ELECTROCARDIOGRAM}")
    fun getAll(): DataSource.Factory<Int, Ecg>

    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.ELECTROCARDIOGRAM}")
    suspend fun allEcg(): MutableList<Ecg>

    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.ELECTROCARDIOGRAM} WHERE has_sync = 0")
    fun getNotSynced(): Flow<MutableList<Ecg>>

    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.ELECTROCARDIOGRAM} WHERE id=:id")
    fun get(id: Long): Flow<Ecg>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ecg: Ecg)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(ecgs: MutableList<Ecg>)

    @Update
    suspend fun update(ecg: Ecg)

    @Query("DELETE FROM ${AppConstants.ELECTROCARDIOGRAM}")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(ecg: Ecg)
}