package com.dhealth.bluetooth.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivity
import com.bezzo.core.extension.launchActivityClearAllStack
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.services.MeasurementService
import com.dhealth.bluetooth.ui.measurement.MeasurementResultActivity
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import kotlinx.android.synthetic.main.activity_measurement_setting.*
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class MeasurementSetting : BaseActivity() {

    private val tempVM: TemperatureViewModel by inject()
    private val measurementVM: MeasurementViewModel by inject()

    private val hrmVM: HrmViewModel by inject()
    private val ecgVM: EcgViewModel by inject()

    private var measurementType = 1

    override fun onInitializedView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)

        tv_device_info.text = measurementVM.selectedDevice()

        setMeasurementType()
        setMonitoringStatus()

        fab_history.setOnClickListener { launchActivity<MeasurementResultActivity>() }

        when(measurementVM.measurementType()){
            1 -> rb_temperature.isChecked = true
            2 -> rb_heart_rate.isChecked = true
            3 -> rb_ecg.isChecked = true
        }
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
        mb_start.setOnClickListener {
            val intent = Intent(this, MeasurementService::class.java)
            intent.action = MeasurementService.ACTION_START_FOREGROUND_SERVICE
            measurementVM.measurementType(measurementType)
            startService(intent)
        }

        mb_stop.setOnClickListener {
            val intent = Intent(this, MeasurementService::class.java)
            intent.action = MeasurementService.ACTION_STOP_FOREGROUND_SERVICE
            startService(intent)

            exitProcess(0)
            launchActivityClearAllStack<ScanActivity>()
        }
    }
}
