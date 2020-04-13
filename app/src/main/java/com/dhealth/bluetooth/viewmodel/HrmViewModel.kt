package com.dhealth.bluetooth.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.bezzo.core.base.BaseViewModel
import com.dhealth.bluetooth.data.local.LocalStorage
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.repository.HrmRepository
import com.dhealth.bluetooth.util.Loading
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.util.measurement.HrmUtil
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

    fun share(context: Context): LiveData<ShareState> {
        val state = MutableLiveData<ShareState>()
        state.postValue(Loading)
        if(!saveHrm(context).start()){
            state.postValue(Saved)
        }

        return state
    }

    private fun saveHrm(context: Context) = viewModelScope.launch {
        StorageUtil.saveHrm(context, HrmUtil.hrmToJson(repository.allHrm()))
    }
}