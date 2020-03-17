package com.dhealth.personalasssesment.di

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.personalasssesment.adapter.BluetoothDeviceRVAdapter
import com.dhealth.personalasssesment.adapter.ScanResultRVAdapter
import org.koin.dsl.module

val appModule = module {
    single { SessionHelper() }
}

val viewModelModule = module {

}

val rvAdapterModule = module {
    factory { BluetoothDeviceRVAdapter(ArrayList()) }
    factory { ScanResultRVAdapter(ArrayList()) }
}

val allModule = listOf(
    appModule,
    viewModelModule,
    rvAdapterModule
)