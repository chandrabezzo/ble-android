package com.dhealth.bluetooth.di

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.adapter.BluetoothDeviceRVAdapter
import com.dhealth.bluetooth.adapter.ScanResultRVAdapter
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