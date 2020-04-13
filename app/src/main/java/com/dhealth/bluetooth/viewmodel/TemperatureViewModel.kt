package com.dhealth.bluetooth.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.data.repository.TemperatureRepository
import com.dhealth.bluetooth.util.Loading
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.util.measurement.TemperatureUtil
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

    fun share(context: Context): LiveData<ShareState> {
        val state = MutableLiveData<ShareState>()
        state.postValue(Loading)
        if(!saveTemperature(context).start()){
            state.postValue(Saved)
        }

        return state
    }

    private fun saveTemperature(context: Context) = viewModelScope.launch {
        StorageUtil.saveTemperature(context, TemperatureUtil.temperatureToJson(repository.allTemperature()))
    }

    fun add(temperature: Temperature) = viewModelScope.launch { repository.insert(temperature) }

    fun inserts(values: ArrayList<Temperature>) = viewModelScope.launch { repository.inserts(values) }
}