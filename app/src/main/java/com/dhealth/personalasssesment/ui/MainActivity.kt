package com.dhealth.personalasssesment.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.dhealth.personalasssesment.R

class MainActivity : BaseActivity() {

    companion object {
        const val REQUEST_BT = 1
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bluetoothAdapter.takeIf { it.isEnabled }.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,
                REQUEST_BT
            )
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_main
    }
}
