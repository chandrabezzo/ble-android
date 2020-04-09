package com.dhealth.bluetooth.data.local.dao

import androidx.annotation.WorkerThread
import androidx.paging.DataSource
import androidx.room.*
import com.dhealth.bluetooth.data.constant.AppConstants
import com.dhealth.bluetooth.data.model.Hrm
import kotlinx.coroutines.flow.Flow

@Dao
interface HrmDao {
    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.HEART_RATE}")
    fun getAll(): DataSource.Factory<Int, Hrm>

    @WorkerThread
    @Query("SELECT * FROM ${AppConstants.HEART_RATE} WHERE id=:id")
    fun get(id: Long): Flow<Hrm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hrm: Hrm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(values: MutableList<Hrm>)

    @Query("DELETE FROM ${AppConstants.HEART_RATE}")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(hrm: Hrm)
}