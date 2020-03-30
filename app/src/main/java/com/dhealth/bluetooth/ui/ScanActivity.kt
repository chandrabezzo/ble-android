package com.dhealth.bluetooth.ui

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.*
import com.bezzo.core.listener.OnItemClickListener
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDeviceRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.GpsUtil
import com.dhealth.bluetooth.util.PermissionUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_scan.*
import org.koin.android.ext.android.inject

class ScanActivity : BaseActivity() {

    private val scanPeriod: Long = 1000
    private var mScanning = false
    private var mHandler = Handler()
    private val bleDeviceAdapter: BleDeviceRVAdapter by inject()
    private lateinit var selectedDevice: BleDevice

    companion object {
        const val REQUEST_BT = 1
    }

    private val bleAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onInitializedView(savedInstanceState: Bundle?) {
        if(PermissionUtil.requestFineLocationPermission(this)){
            if(bleAdapter.isEnabled){
                if(!GpsUtil(this).isActive(this)){
                    GpsUtil(this).checkStatusGPS(this)
                }
                else {
                    selectDevice()
                    scanLeDevice(bleAdapter.isEnabled)
                }
            }
            else {
                requestEnableBle()
                if(!GpsUtil(this).isActive(this)){
                    GpsUtil(this).checkStatusGPS(this)
                }
                else {
                    selectDevice()
                    scanLeDevice(bleAdapter.isEnabled)
                }
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rv_device.layoutManager = layoutManager
        rv_device.adapter = bleDeviceAdapter

        sr_device.setOnRefreshListener {
            scanLeDevice(bleAdapter.isEnabled)
            sr_device.isRefreshing = false
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_scan
    }

    private fun requestEnableBle(){
        bleAdapter.takeIf { it.isEnabled }.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_BT)
        }
    }

    private fun scanLeDevice(isEnable: Boolean){
        if(Build.VERSION.SDK_INT > 21){
            val leScanner = bleAdapter.bluetoothLeScanner
            if(isEnable){
                mHandler.postDelayed({
                    mScanning = false
                    leScanner.stopScan(scanCallback)
                }, scanPeriod)
                mScanning = true
                leScanner.startScan(scanCallback)
            }
            else {
                mScanning = false
                leScanner.stopScan(scanCallback)
            }
        }
        else {
            if(isEnable){
                mHandler.postDelayed({
                    mScanning = false
                    bleAdapter.stopLeScan(leScanCallback)
                }, scanPeriod)
                mScanning = true
            }
            else {
                mScanning = false
                bleAdapter.stopLeScan(leScanCallback)
            }
        }
    }

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            bleDeviceAdapter.addItem(BleDevice(device, rssi, scanRecord))
            bleDeviceAdapter.notifyDataSetChanged()
        }
    }

    private val scanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let {
                val devices: MutableList<BleDevice> = mutableListOf()
                for(result in it){
                    val bleDevice = BleDevice(result.device, result.rssi, result.scanRecord?.bytes)
                    if(!devices.contains(bleDevice)) devices.add(bleDevice)
                }

                bleDeviceAdapter.setItems(devices)
                bleDeviceAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val bleDevice = BleDevice(it.device, it.rssi, it.scanRecord?.bytes)
                if(!bleDeviceAdapter.getItems().contains(bleDevice)) bleDeviceAdapter.addItem(bleDevice)
                bleDeviceAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Snackbar.make(content, "Gagal Scan", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun selectDevice(){
        bleDeviceAdapter.setOnItemClick(object : OnItemClickListener{
            override fun onItemClick(itemView: View, position: Int) {
                selectedDevice = bleDeviceAdapter.getItem(position)
                selectedDevice.device.connectGatt(this@ScanActivity,
                    true, gattCallback)
                connecting()
            }

            override fun onItemLongClick(itemView: View, position: Int): Boolean {
                return true
            }
        })
    }

    private val gattCallback = object: BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                launchActivity<ActionActivity>{
                    putExtra(Extras.BLE_DEVICE, selectedDevice)
                }
            }
        }
    }

    private fun connecting(){
        pb_loading.show()
        sr_device.hide()
    }
}