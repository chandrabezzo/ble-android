package com.dhealth.bluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.repository.EcgRepository
import kotlinx.coroutines.launch

class EcgViewModel(application: Application): BaseViewModel(application) {
    private var repository: EcgRepository

    init {
        val dao = LocalStorage.getDatabase(application, viewModelScope)
            .ecgDao()
        repository = EcgRepository(dao)
    }

    fun getAll(): LiveData<PagedList<Ecg>> {
        return repository.getAll()
    }

    fun add(ecg: Ecg) = viewModelScope.launch { repository.insert(ecg) }

    fun inserts(values: ArrayList<Ecg>) = viewModelScope.launch { repository.inserts(values) }
}