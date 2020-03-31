package com.dhealth.bluetooth.di

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.adapter.BleDeviceRVAdapter
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { SessionHelper() }
    single { RxBleClient.create(androidContext()) }
    single { CompositeDisposable() }
}

val viewModelModule = module {

}

val rvAdapterModule = module {
    factory { BleDeviceRVAdapter(ArrayList()) }
    factory { BleDataRVAdapter(ArrayList()) }
}

val allModule = listOf(
    appModule,
    viewModelModule,
    rvAdapterModule
)