package com.dhealth.bluetooth.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.bezzo.core.BuildConfig
import com.bezzo.core.extension.toast
import com.bezzo.core.util.TelegramLogger
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.dhealth.bluetooth.util.measurement.TemperatureCallback
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.koin.core.KoinComponent
import org.koin.core.inject

class MeasurementJobIntentService: JobIntentService(), KoinComponent {

    private val viewmodel: MeasurementViewModel by inject()
    private val tempVM: TemperatureViewModel by inject()

    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable
    private var startMeasurement = false

    companion object {
        const val JOB_ID = 1000

        fun enqueue(context: Context, work: Intent){
            enqueueWork(context, MeasurementJobIntentService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        when(viewmodel.measurementType()){
            1 -> startTemp()
            2 -> startHrm()
            3 -> startEcg()
        }
    }

    private fun stopMeasurement(){
        viewmodel.stopMonitoring(connection)
        connectionDisposable.dispose()
        toast("Stop Measurement")
    }

    private fun startTemp(){
        viewmodel.monitoringTemperature(connection, object: TemperatureCallback {
            override fun originalData(values: ByteArray) {
                Log.i("Original Data", values.contentToString())
            }

            override fun temperature(id: Long, celcius: Float, fahrenheit: Float) {
                Log.i("Temperature", "$celcius °C | $fahrenheit °F")
                if(!startMeasurement) toast("Start Measurement")
                startMeasurement = true

                if(!viewmodel.isChecking()) stopMeasurement(); stopSelf()
                tempVM.add(Temperature(celcius, fahrenheit, MeasurementUtil.getEpoch()))
            }

            override fun onError(error: Throwable) {
                TelegramLogger(this@MeasurementJobIntentService, BuildConfig.TELEGRAM_BOT, error.message,
                    getString(R.string.developer_id), BuildConfig.TELEGRAM_ID).send()
            }
        })
    }

    private fun startHrm(){

    }

    private fun startEcg(){

    }
}