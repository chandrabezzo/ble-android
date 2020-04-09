package com.dhealth.bluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.data.repository.TemperatureRepository
import kotlinx.coroutines.launch

class TemperatureViewModel(application: Application): BaseViewModel(application) {
    private var repository: TemperatureRepository

    init {
        val dao = LocalStorage.getDatabase(application, viewModelScope)
            .temperatureDao()
        repository = TemperatureRepository(dao)
    }

    fun getAll(): LiveData<PagedList<Temperature>> {
        return repository.getAll()
    }

    fun add(temperature: Temperature) = viewModelScope.launch { repository.insert(temperature) }

    fun inserts(values: ArrayList<Temperature>) = viewModelScope.launch { repository.inserts(values) }
}