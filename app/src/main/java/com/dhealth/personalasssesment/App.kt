package com.dhealth.personalasssesment

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.bezzo.core.CoreModul
import com.bezzo.core.data.session.SessionHelper
import com.dhealth.personalasssesment.di.allModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(allModule)
        }

        CoreModul.Builder(this).build()
        SessionHelper().instance(this)
    }
}