package com.dhealth.bluetooth.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dhealth.bluetooth.data.local.dao.HrmDao
import com.dhealth.bluetooth.data.model.Hrm
import kotlinx.coroutines.flow.Flow

class HrmRepository(private val dao: HrmDao) {
    fun getAll(): LiveData<PagedList<Hrm>> {
        return LivePagedListBuilder(dao.getAll(),
            20).build()
    }

    fun get(id: Long): Flow<Hrm> {
        return dao.get(id)
    }

    suspend fun insert(hrm: Hrm) {
        dao.insert(hrm)
    }

    suspend fun inserts(values: ArrayList<Hrm>){
        dao.inserts(values)
    }

    suspend fun delete(hrm: Hrm){
        dao.delete(hrm)
    }
}