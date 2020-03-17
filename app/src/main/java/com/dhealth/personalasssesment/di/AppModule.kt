package com.dhealth.personalasssesment.di

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.personalasssesment.adapter.DeviceRVAdapter
import org.koin.dsl.module

val appModule = module {
    single { SessionHelper() }
}

val viewModelModule = module {

}

val rvAdapterModule = module {
    factory { DeviceRVAdapter(ArrayList()) }
}

val allModule = listOf(
    appModule,
    viewModelModule,
    rvAdapterModule
)