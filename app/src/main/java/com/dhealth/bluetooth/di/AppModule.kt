package com.dhealth.bluetooth.di

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.adapter.*
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SessionHelper() }
    single { RxBleClient.create(androidContext()) }
    single { CompositeDisposable() }
}

val viewModelModule = module {
    viewModel { TemperatureViewModel(androidApplication()) }
    viewModel { HrmViewModel(androidApplication()) }
    viewModel { EcgViewModel(androidApplication()) }
}

val rvAdapterModule = module {
    factory { BleDeviceRVAdapter(ArrayList()) }
    factory { BleDataRVAdapter(ArrayList()) }
    factory { TempMeasurementRVAdapter() }
    factory { HrmMeasurementRVAdapter() }
    factory { EcgMeasurementRVAdapter() }
}

val allModule = listOf(
    appModule,
    viewModelModule,
    rvAdapterModule
)