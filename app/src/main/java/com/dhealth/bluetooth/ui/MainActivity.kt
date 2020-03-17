package com.dhealth.bluetooth.ui

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private var bleDevice: BluetoothDevice? = null

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)
        tv_device.text = "Connected with: ${bleDevice?.name} (${bleDevice?.address})"
    }

    override fun setLayout(): Int {
        return R.layout.activity_main
    }
}
