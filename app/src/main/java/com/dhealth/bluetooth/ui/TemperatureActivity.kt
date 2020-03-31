package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.dhealth.bluetooth.util.measurement.TemperatureCallback
import com.dhealth.bluetooth.util.measurement.TemperatureUtil
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_temperature.*
import org.koin.android.ext.android.inject


class TemperatureActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val adapter: BleDataRVAdapter by inject()
    private val compositeDisposable: CompositeDisposable by inject()
    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

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

        doMeasurement()
    }

    override fun setLayout(): Int {
        return R.layout.activity_temperature
    }

    override fun onDestroy() {
        MeasurementUtil.commandStop(compositeDisposable, connection)
        compositeDisposable.dispose()
        connectionDisposable.dispose()
        super.onDestroy()
    }

    private fun doMeasurement(){
        TemperatureUtil.commandInterval(compositeDisposable, connection, 500)
        TemperatureUtil.commandReadTemp(compositeDisposable, connection)
        adapter.addData("## PEMERIKSAAN SUHU ##")
        TemperatureUtil.commandGetTemperature(compositeDisposable, connection, object : TemperatureCallback {
            override fun originalData(values: ByteArray) {
                Log.i("Data Notification", values.contentToString())
                runOnUiThread {
                    adapter.addData("Original: ${values.contentToString()}")
                    adapter.notifyDataSetChanged()
                }
            }

            override fun temperatureInCelcius(value: Float) {
                Log.i("Suhu", MeasurementUtil.decimalFormat(value))
                runOnUiThread {
                    adapter.addData("Celcius: $value")
                    adapter.notifyDataSetChanged()
                }
            }

            override fun temperatureInFahrenheit(value: Float) {
                Log.i("Suhu Fahrenheit", MeasurementUtil.decimalFormat(value))
                runOnUiThread {
                    adapter.addData("Fahrenheit: $value")
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onError(error: Throwable) {
                Log.e("Error Notification", error.localizedMessage)
            }
        })
    }
}