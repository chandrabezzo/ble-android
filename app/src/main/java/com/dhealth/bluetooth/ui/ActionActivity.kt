package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.ui.measurement.MeasurementResultActivity
import com.dhealth.bluetooth.util.PermissionUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_action.*
import org.koin.android.ext.android.inject

class ActionActivity : BaseActivity() {

    private val viewModel: MeasurementViewModel by inject()

    override fun onInitializedView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)

        cv_electrocardiogram.setOnClickListener {
            if(isPermissionGranted()){
                launchActivity<ElectrocardiogramActivity>()
            }
        }

        cv_optical_hrm.setOnClickListener {
            if(isPermissionGranted()){
                launchActivity<OpticalHrmActivity>()
            }
        }

        cv_temperature.setOnClickListener {
            if(isPermissionGranted()){
                launchActivity<TemperatureActivity>()
            }
        }

        cv_background_monitoring.setOnClickListener {
            if(isPermissionGranted()){
                launchActivity<MeasurementSetting>()
            }
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_action
    }

    private fun isPermissionGranted(): Boolean {
        return PermissionUtil.requestWriteStorage(this) and PermissionUtil.requestReadStorage(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scan_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_history -> launchActivity<MeasurementResultActivity>()
        }

        return super.onOptionsItemSelected(item)
    }
}
