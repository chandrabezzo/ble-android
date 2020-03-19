package com.dhealth.bluetooth.ui

import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private val adapter: BleDataRVAdapter by inject()

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        val layoutManager = LinearLayoutManager(this)
        rv_device_data.layoutManager = layoutManager
        rv_device_data.adapter = adapter

        bleDevice?.device?.connectGatt(this, true, gattCallback)?.let {
            bluetoothGatt = it
        }

        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

        adapter.addData("## DEVICE ##")
        adapter.addData("UUID: ${bleDevice?.device?.uuids.toString()}")
        adapter.addData("RSSI: ${bleDevice?.rssi}")
        adapter.addData("Scan Record: ${bleDevice?.scanRecord?.contentToString()}")
        adapter.addData("Device Class: ${bleDevice?.device?.bluetoothClass?.deviceClass}")
        adapter.addData("Major Device Class: ${bleDevice?.device?.bluetoothClass?.majorDeviceClass}")
        bleDevice?.device?.bondState?.let { bondState ->
            adapter.addData("Bond State: ${BleUtil.bondState(bondState)}")
        }
        bleDevice?.device?.type?.let { type ->
            adapter.addData("Device Type: ${BleUtil.type(type)}")
        }
        adapter.addData("Fetch UUID With SDP: ${bleDevice?.device?.fetchUuidsWithSdp()}")

        adapter.notifyDataSetChanged()
    }

    override fun setLayout(): Int {
        return R.layout.activity_main
    }

    override fun onDestroy() {
        bluetoothGatt.close()
        bluetoothGatt.disconnect()
        super.onDestroy()
    }

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.services?.let { displayGattServices(gatt, it) }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.i("## Mulai ##", "On Characteristic Changed")
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }
    }

    private fun displayGattServices(gatt: BluetoothGatt, gattServices: MutableList<BluetoothGattService>){
        adapter.addData("## Gatt Service ##")

        for(service in gattServices){
            adapter.addData("UUID: ${service.uuid}")
            adapter.addData("Instance ID: ${service.instanceId}")
            adapter.addData("Service Type: ${service.type}")

            runOnUiThread { adapter.notifyDataSetChanged() }
        }

        for(service in gattServices){
            displayGattCharacteristics(gatt, service.characteristics)
        }
    }

    private fun displayGattCharacteristics(gatt: BluetoothGatt, gattCharacteristics: MutableList<BluetoothGattCharacteristic>){
        adapter.addData("## Gatt Characteristic ##")

        for(characteristic in gattCharacteristics){
            gatt.setCharacteristicNotification(characteristic, true)

            adapter.addData("Characteristic Value: ${characteristic.value}")
            adapter.addData("Instance ID: ${characteristic.instanceId}")
            adapter.addData("Permission: ${BleUtil.characteristicPermission(characteristic.permissions)}")
            adapter.addData("Properties: ${BleUtil.characteristicProperty(characteristic.properties)}")
            adapter.addData("UUID: ${characteristic.uuid}")
            adapter.addData("Value: ${characteristic.value?.contentToString()}")

            runOnUiThread { adapter.notifyDataSetChanged() }
        }

        for(characteristic in gattCharacteristics){
            displayDescriptor(gatt, characteristic.descriptors)
        }
    }

    private fun displayDescriptor(gatt: BluetoothGatt, gattDescriptors: MutableList<BluetoothGattDescriptor>){
        adapter.addData("## Gatt Descriptor ##")

        for(descriptor in gattDescriptors){
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)

            adapter.addData("UUID: ${descriptor.uuid}")
            adapter.addData("Permission: ${descriptor.permissions}")
            adapter.addData("Value: ${descriptor.value?.contentToString()}")

            runOnUiThread { adapter.notifyDataSetChanged() }
        }
    }
}
