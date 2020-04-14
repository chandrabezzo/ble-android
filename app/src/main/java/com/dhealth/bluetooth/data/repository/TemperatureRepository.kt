package com.dhealth.bluetooth.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dhealth.bluetooth.data.local.dao.TemperatureDao
import com.dhealth.bluetooth.data.model.Temperature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemperatureRepository(private val dao: TemperatureDao) {


    fun getAll(): LiveData<PagedList<Temperature>> {
        return LivePagedListBuilder(dao.getAll(),
            20).build()
    }

    suspend fun allTemperature(): MutableList<Temperature> {
        return dao.allTemperature()
    }

    fun notSynced(): Flow<MutableList<Temperature>> {
        return dao.getNotSynced().map { it }
    }

    fun get(id: Long): Flow<Temperature> {
        return dao.get(id)
    }

    suspend fun insert(temperature: Temperature) {
        dao.insert(temperature)
    }

    suspend fun inserts(values: ArrayList<Temperature>){
        dao.inserts(values)
    }

    suspend fun update(temperature: Temperature){
        dao.update(temperature)
    }

    suspend fun delete(temperature: Temperature){
        dao.delete(temperature)
    }
}