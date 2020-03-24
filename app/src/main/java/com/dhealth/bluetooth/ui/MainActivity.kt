package com.dhealth.bluetooth.ui

import android.bluetooth.*
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
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

        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

        bleDevice?.device?.connectGatt(this, true, gattCallback)?.let {
            bluetoothGatt = it
        }

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

        sr_data.setOnRefreshListener {
            val characteristic = bluetoothGatt.getService(Maxim.service).getCharacteristic(Maxim.dataCharacteristic)
            bluetoothGatt.readCharacteristic(characteristic)
        }
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
                bluetoothGatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            bluetoothGatt.services?.let { displayGattServices(it) }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            displayDescriptor(characteristic.descriptors)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            displayDescriptor(characteristic.descriptors)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            val characteristic = bluetoothGatt.getService(Maxim.service).getCharacteristic(Maxim.dataCharacteristic)
            bluetoothGatt.readCharacteristic(characteristic)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            val characteristic = bluetoothGatt.getService(Maxim.service).getCharacteristic(Maxim.rawDataCharacteristic)
            characteristic.value = byteArrayOf(0, 170.toByte())
            bluetoothGatt.writeCharacteristic(characteristic)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
        }

        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }
    }

    private fun displayGattServices(gattServices: MutableList<BluetoothGattService>){
        adapter.addData("## Gatt Service ##")

        for(service in gattServices){
            adapter.addData("UUID: ${service.uuid}")
            adapter.addData("Instance ID: ${service.instanceId}")
            adapter.addData("Service Type: ${service.type}")

            runOnUiThread { adapter.notifyDataSetChanged() }
        }

        for(service in gattServices){
            displayGattCharacteristics(service.characteristics)
        }
    }

    private fun displayGattCharacteristics(gattCharacteristics: MutableList<BluetoothGattCharacteristic>){
        adapter.addData("## Gatt Characteristic ##")

        for(characteristic in gattCharacteristics){
            if(characteristic.uuid == Maxim.dataCharacteristic){
                adapter.addData("Characteristic Value: ${characteristic.value}")
                adapter.addData("Instance ID: ${characteristic.instanceId}")
                adapter.addData("Permission: ${BleUtil.characteristicPermission(characteristic.permissions)}")
                adapter.addData("Properties: ${BleUtil.characteristicProperty(characteristic.properties)}")
                adapter.addData("UUID: ${characteristic.uuid}")
                adapter.addData("Value: ${characteristic.value?.contentToString()}")

                runOnUiThread { adapter.notifyDataSetChanged() }

                bluetoothGatt.setCharacteristicNotification(characteristic, true)
                displayDescriptor(characteristic.descriptors)
            }
            else {
                bluetoothGatt.setCharacteristicNotification(characteristic, false)
            }
        }
    }

    private fun displayDescriptor(gattDescriptors: MutableList<BluetoothGattDescriptor>){
        adapter.addData("## Gatt Descriptor ##")

        for(descriptor in gattDescriptors){
            if(descriptor.uuid == Maxim.descriptor){
                adapter.addData("UUID: ${descriptor.uuid}")
                adapter.addData("Permission: ${descriptor.permissions}")
                adapter.addData("Value: ${descriptor.value?.contentToString()}")

                runOnUiThread { adapter.notifyDataSetChanged() }

                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                bluetoothGatt.writeDescriptor(descriptor)
            }
        }

        sr_data.isRefreshing = false
    }
}
