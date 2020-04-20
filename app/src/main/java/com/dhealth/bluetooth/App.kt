package com.dhealth.bluetooth

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.bezzo.core.CoreModul
import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.di.allModule
import com.parse.Parse
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("com.dhealth.bluetooth") // if defined
                .clientKey("MuJiCz/eeCAj44f0neyc5IMYWP2Lz+wonQY7i5S0x5M=")
                .server("http://parse.dev.dhealth.co.id/parse/")
                .build()
        )

        startKoin {
            androidContext(this@App)
            modules(allModule)
        }

        CoreModul.Builder(this).build()
        SessionHelper().instance(this)
    }
}