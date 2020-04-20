package com.dhealth.bluetooth.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
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

class MeasurementService(private val context: Context,
                         private val params: WorkerParameters)
    : Worker(context, params), KoinComponent {

    private val viewmodel: MeasurementViewModel by inject()
    private val tempVM: TemperatureViewModel by inject()

    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable

    override fun doWork(): Result {
        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        when(viewmodel.measurementType()){
            1 -> startTemp()
            2 -> startHrm()
            3 -> startEcg()
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        viewmodel.stopMonitoring(connection)
        connectionDisposable.dispose()
    }

    private fun startTemp(){
        viewmodel.monitoringTemperature(connection, object: TemperatureCallback{
            override fun originalData(values: ByteArray) {
                Log.i("Original Data", values.contentToString())
            }

            override fun temperature(id: Long, celcius: Float, fahrenheit: Float) {
                Log.i("Temperature", "$celcius °C | $fahrenheit °F")
                if(!viewmodel.isChecking()){
                    viewmodel.isChecking(true)
                    context.toast("Start Pemeriksaan")
                }
                tempVM.add(Temperature(celcius, fahrenheit, MeasurementUtil.getEpoch()))
            }

            override fun onError(error: Throwable) {
                TelegramLogger(context, BuildConfig.TELEGRAM_BOT, error.message,
                    context.getString(R.string.developer_id), BuildConfig.TELEGRAM_ID).send()
            }

        })
    }

    private fun startHrm(){

    }

    private fun startEcg(){

    }
}