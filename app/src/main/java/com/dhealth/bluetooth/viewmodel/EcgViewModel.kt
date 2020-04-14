package com.dhealth.bluetooth.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.repository.EcgRepository
import com.dhealth.bluetooth.util.Prepare
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.util.measurement.EcgUtil
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

    private fun saveEcg(context: Context) = viewModelScope.launch {
        StorageUtil.saveEcg(context, EcgUtil.ecgToJson(repository.allEcg()))
    }

    fun share(context: Context): LiveData<ShareState> {
        val state = MutableLiveData<ShareState>()
        state.postValue(Prepare)
        if(!saveEcg(context).start()){
            state.postValue(Saved)
        }

        return state
    }

    fun synced(ecg: Ecg) = viewModelScope.launch {
        ecg.hasSync = 1
        repository.update(ecg)
    }

    fun sync(): LiveData<MutableList<Ecg>> {
        return repository.getNotSynced().asLiveData()
    }
}