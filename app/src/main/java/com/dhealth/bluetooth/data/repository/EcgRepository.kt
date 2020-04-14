package com.dhealth.bluetooth.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dhealth.bluetooth.data.local.dao.EcgDao
import com.dhealth.bluetooth.data.model.Ecg
import kotlinx.coroutines.flow.Flow

class EcgRepository(private val dao: EcgDao) {
    fun getAll(): LiveData<PagedList<Ecg>> {
        return LivePagedListBuilder(dao.getAll(),
            20).build()
    }

    suspend fun allEcg(): MutableList<Ecg> {
        return dao.allEcg()
    }

    fun getNotSynced(): Flow<MutableList<Ecg>> {
        return dao.getNotSynced()
    }

    fun get(id: Long): Flow<Ecg> {
        return dao.get(id)
    }

    suspend fun insert(temperature: Ecg) {
        dao.insert(temperature)
    }

    suspend fun inserts(values: ArrayList<Ecg>){
        dao.inserts(values)
    }

    suspend fun update(ecg: Ecg){
        dao.update(ecg)
    }

    suspend fun delete(ecg: Ecg){
        dao.delete(ecg)
    }
}