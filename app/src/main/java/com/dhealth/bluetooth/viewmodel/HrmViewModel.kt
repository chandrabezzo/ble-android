package com.dhealth.bluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.repository.HrmRepository
import kotlinx.coroutines.launch

class HrmViewModel(application: Application): BaseViewModel(application) {
    private var repository: HrmRepository

    init {
        val dao = LocalStorage.getDatabase(application, viewModelScope)
            .hrmDao()
        repository = HrmRepository(dao)
    }

    fun getAll(): LiveData<PagedList<Hrm>> {
        return repository.getAll()
    }

    fun add(hrm: Hrm) = viewModelScope.launch { repository.insert(hrm) }

    fun inserts(values: ArrayList<Hrm>) = viewModelScope.launch { repository.inserts(values) }
}