package com.dhealth.bluetooth.ui

import android.bluetooth.*
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.Commands
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.observers.SubscriberCompletableObserver
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.reactivestreams.Subscriber
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8


class MainActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private val adapter: BleDataRVAdapter by inject()
    private val bleClient: RxBleClient by inject()
    private val compositeDisposable = CompositeDisposable()

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        val layoutManager = LinearLayoutManager(this)
        rv_device_data.layoutManager = layoutManager
        rv_device_data.adapter = adapter

        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

//        bleDevice?.device?.connectGatt(this, true, gattCallback)?.let {
//            bluetoothGatt = it
//        }

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
            bluetoothGatt.discoverServices()
        }

        val connection = bleClient.getBleDevice(bleDevice?.device?.address!!)
            .establishConnection(true).compose(ReplayingShare.instance())
//        val commandsStreamType = sendCommand(Commands.setStreamTypeToBinary)
//        val commandsTimeCommand = sendCommand(Commands.createSetTimeCommand())
        val commandsInterval = sendCommand(Commands.createTempSampleIntervalCommand(500))
        val commandsReadTemp = sendCommand(Commands.readTemp)

//        for (command in commandsTimeCommand){
//            compositeDisposable.add(connection.flatMap {
//                it.writeCharacteristic(Maxim.rawDataCharacteristic, command).toObservable()
//            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
//                Log.i("Data Time Command", it?.contentToString())
//            }, {
//                Log.e("Error Time Command", it.localizedMessage)
//            }))
//        }
//
//        for(command in commandsStreamType){
//            compositeDisposable.add(connection.flatMap {
//                it.writeCharacteristic(Maxim.rawDataCharacteristic, command).toObservable()
//            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
//                Log.i("Data Stream Type", it?.contentToString())
//            }, {
//                Log.e("Error Stream Type", it.localizedMessage)
//            }))
//        }

        compositeDisposable.add(connection.flatMap {
            commandsInterval.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Interval", it?.contentToString())
        }, {
            Log.e("Error Interval", it.localizedMessage)
        }))

        compositeDisposable.add(connection.flatMap {
            commandsReadTemp.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Temp", it?.contentToString())
        }, {
            Log.e("Error Read Temp", it.localizedMessage)
        }))

        compositeDisposable.add(connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                Log.i("Data Notification", data?.contentToString())
                runOnUiThread { toast("Data Notif: ${data?.contentToString()}") }
            }, {
                Log.e("Error Notification", it.localizedMessage)
            }))
    }

    private fun sendCommand(str: String): ArrayList<ByteArray> {
        var bArr: ByteArray?
        val sb = StringBuilder()
        sb.append(str)
        sb.append("\n")
        val chunked = sb.chunked(16)
        val arrayList = ArrayList<ByteArray>()
        for (str2 in chunked) {
            val length = 16 - str2.length
            when {
                length == 0 -> {
                    val charset: Charset = UTF_8
                    bArr = str2.toByteArray(charset)
                }
                length >= 0 -> {
                    val bArr2 = ByteArray(16)
                    val charSequence: CharSequence = str2
                    var i = 0
                    var i2 = 0
                    while (i < charSequence.length) {
                        val i3 = i2 + 1
                        bArr2[i2] = charSequence[i].toByte()
                        i++
                        i2 = i3
                    }
                    for (length2 in str2.length..15) {
                        bArr2[length2] = 0
                    }
                    bArr = bArr2
                }
                else -> {
                    val sb2 = StringBuilder()
                    sb2.append("String is bigger than ")
                    sb2.append(16)
                    sb2.append(" bytes")
                    throw IllegalArgumentException(sb2.toString())
                }
            }
            arrayList.add(bArr)
        }

        return arrayList
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
            gatt?.services?.let { displayGattServices(it) }
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
            adapter.addData("On Characteristic Read")
            adapter.addData("Value: ${characteristic.value.contentToString()}")
            runOnUiThread { adapter.notifyDataSetChanged() }
            displayDescriptor(characteristic.descriptors)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            adapter.addData("On Characteristic Write")
            adapter.addData("Value: ${characteristic?.value?.contentToString()}")
            runOnUiThread { adapter.notifyDataSetChanged() }
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
            super.onDescriptorWrite(gatt, descriptor, status)
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

                sendCommand(Commands.createTempSampleIntervalCommand(500))
                bluetoothGatt.setCharacteristicNotification(characteristic, true)

                displayDescriptor(characteristic.descriptors)
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