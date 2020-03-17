package com.dhealth.personalasssesment.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.bezzo.core.base.BaseActivity
import com.dhealth.personalasssesment.R
import com.dhealth.personalasssesment.adapter.DeviceRVAdapter
import org.koin.android.ext.android.inject

class ScanActivity : BaseActivity() {

    private val SCAN_PERIOD: Long = 1000
    private var mScanning = false
    private var mHandler = Handler()
    private val adapter: DeviceRVAdapter by inject()

    private val bleAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onInitializedView(savedInstanceState: Bundle?) {
        requestEnableBle()
        bleAdapter.startDiscovery()
        scanLeDevice(bleAdapter.isEnabled)
    }

    override fun setLayout(): Int {
        return R.layout.activity_scan
    }

    private fun requestEnableBle(){
        bleAdapter.takeIf { it.isEnabled }.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,
                MainActivity.REQUEST_BT
            )
        }
    }

    private fun scanLeDevice(isEnable: Boolean){
        if(isEnable){
            mHandler.postDelayed({
                mScanning = false
                bleAdapter.stopLeScan(leScanCallback)
            }, SCAN_PERIOD)
            mScanning = true
            bleAdapter.startLeScan(leScanCallback)
        }
        else {
            mScanning = false
            bleAdapter.stopLeScan(leScanCallback)
        }
    }

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            adapter.addItem(device)
            adapter.notifyDataSetChanged()
        }
    }
}
