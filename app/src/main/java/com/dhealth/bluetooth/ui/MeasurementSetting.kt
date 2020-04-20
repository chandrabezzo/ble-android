package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivity
import com.bezzo.core.extension.snackbar
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.services.MeasurementService
import com.dhealth.bluetooth.ui.measurement.MeasurementResultActivity
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.dhealth.bluetooth.util.measurement.TemperatureCallback
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_measurement_setting.*
import org.koin.android.ext.android.inject

class MeasurementSetting : BaseActivity() {

    private val tempVM: TemperatureViewModel by inject()
    private val measurementVM: MeasurementViewModel by inject()
    private val bleClient: RxBleClient by inject()

    private val hrmVM: HrmViewModel by inject()
    private val ecgVM: EcgViewModel by inject()

    private var measurementType = 1

    private lateinit var connectionDisposable: Disposable
    private lateinit var connection: Observable<RxBleConnection>

    override fun onInitializedView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)

        tv_device_info.text = measurementVM.selectedDevice()

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        setMeasurementType()
        setMonitoringStatus()

        fab_history.setOnClickListener { launchActivity<MeasurementResultActivity>() }

        when(measurementVM.measurementType()){
            1 -> rb_temperature.isChecked = true
            2 -> rb_heart_rate.isChecked = true
            3 -> rb_ecg.isChecked = true
        }

        sw_monitoring.isChecked = measurementVM.isChecking()
    }

    override fun setLayout(): Int {
        return R.layout.activity_measurement_setting
    }

    private fun setMeasurementType(){
        rb_temperature.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) measurementType = 1
        }

        rb_heart_rate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) measurementType = 2
        }

        rb_ecg.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) measurementType = 3
        }
    }

    private fun setMonitoringStatus(){
        sw_monitoring.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                measurementVM.deviceConnect(true)
                if(!measurementVM.isDeviceConnect()) {
                    RxBus.publish(bleClient.getBleDevice(measurementVM.selectedDevice())
                        .establishConnection(true).compose(ReplayingShare.instance()))
                }

                val worker = OneTimeWorkRequest.Builder(MeasurementService::class.java)
                    .build()
                measurementVM.workerId(worker.id)
                WorkManager.getInstance(this).enqueue(worker)

                measurementVM.measurementType(measurementType)
            }
            else {
                measurementVM.stopMonitoring(connection)
                measurementVM.isChecking(false)
                measurementVM.deviceConnect(false)
                WorkManager.getInstance(this).cancelWorkById(measurementVM.workerId())
            }
        }
    }
}
