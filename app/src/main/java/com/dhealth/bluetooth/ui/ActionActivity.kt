package com.dhealth.bluetooth.ui

import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import kotlinx.android.synthetic.main.activity_action.*

class ActionActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        cv_electrocardiogram.setOnClickListener {
            launchActivity<ElectrocardiogramActivity> {
                putExtra(Extras.BLE_DEVICE, bleDevice)
            }
        }

        cv_optical_hrm.setOnClickListener {
            launchActivity<OpticalHrmActivity> {
                putExtra(Extras.BLE_DEVICE, bleDevice)
            }
        }

        cv_temperature.setOnClickListener {
            launchActivity<TemperatureActivity> {
                putExtra(Extras.BLE_DEVICE, bleDevice)
            }
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_action
    }
}
