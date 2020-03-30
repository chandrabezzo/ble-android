package com.dhealth.bluetooth.ui

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.Commands
import com.dhealth.bluetooth.util.TempMapper
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.nio.charset.Charset
import java.text.DecimalFormat
import kotlin.text.Charsets.UTF_8


class MainActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private val adapter: BleDataRVAdapter by inject()
    private val bleClient: RxBleClient by inject()
    private val compositeDisposable = CompositeDisposable()
    lateinit var connection: Observable<RxBleConnection>

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        val layoutManager = LinearLayoutManager(this)
        rv_device_data.layoutManager = layoutManager
        rv_device_data.adapter = adapter

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

        sr_data.setOnRefreshListener {
            bluetoothGatt.discoverServices()
        }

        connection = bleClient.getBleDevice(bleDevice?.device?.address!!)
            .establishConnection(true).compose(ReplayingShare.instance())
        commandGetDeviceInfo(connection)
        commandCreateSetTime(connection)
        commandSetStreamTypeToBin(connection)
        commandInterval(connection, 500)
        commandReadTemp(connection)
        commandSetupNotification(connection)
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
        commandStop(connection)
        super.onDestroy()
    }

    private fun commandGetDeviceInfo(connection: Observable<RxBleConnection>){
        val commandsGetDeviceInfo = sendCommand(Commands.getDeviceInfo)
        compositeDisposable.add(connection.flatMap {
            commandsGetDeviceInfo.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    private fun commandCreateSetTime(connection: Observable<RxBleConnection>){
        val commandsCreateSetTime = sendCommand(Commands.createSetTimeCommand())
        compositeDisposable.add(connection.flatMap {
            commandsCreateSetTime.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    private fun commandSetStreamTypeToBin(connection: Observable<RxBleConnection>){
        val commandsSetStreamTypeToBin = sendCommand(Commands.setStreamTypeToBinary)
        compositeDisposable.add(connection.flatMap {
            commandsSetStreamTypeToBin.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Stream Type 2 Bin", it?.contentToString())
        }, {
            Log.e("Error Stream Type 2 Bin", it.localizedMessage)
        }))
    }

    private fun commandInterval(connection: Observable<RxBleConnection>, interval: Int){
        val commandsInterval = sendCommand(Commands.createTempSampleIntervalCommand(interval))
        compositeDisposable.add(connection.flatMap {
            commandsInterval.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Interval", it?.contentToString())
        }, {
            Log.e("Error Interval", it.localizedMessage)
        }))
    }

    private fun commandReadTemp(connection: Observable<RxBleConnection>){
        val commandsReadTemp = sendCommand(Commands.readTemp)

        compositeDisposable.add(connection.flatMap {
            commandsReadTemp.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Temp", it?.contentToString())
        }, {
            Log.e("Error Read Temp", it.localizedMessage)
        }))
    }

    private fun commandSetupNotification(connection: Observable<RxBleConnection>){
        compositeDisposable.add(connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                val temp = TempMapper.map(data)
                Log.i("Data Notification", data.contentToString())
                Log.i("Suhu", decimalFormat(temp.temperature))
                val fahrenheit = temperatureToFahrenheit(temp.temperature)
                Log.i("Suhu Fahrenheit", decimalFormat(fahrenheit))
                Log.i("Suhu Celcius", decimalFormat(temperatureToCelcius(fahrenheit)))
            }, {
                Log.e("Error Notification", it.localizedMessage)
            }))
    }

    private fun commandStop(connection: Observable<RxBleConnection>){
        val commandStop = sendCommand(Commands.stop)
        compositeDisposable.add(connection.flatMap {
            commandStop.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    private fun decimalFormat(value: Float): String {
        return DecimalFormat("#.00").format(value)
    }

    private fun temperatureToCelcius(value: Float): Float {
        return (value - 32.toFloat()) / 1.8f
    }

    private fun temperatureToFahrenheit(value: Float): Float {
        return value * 1.8f + 32.toFloat()
    }
}