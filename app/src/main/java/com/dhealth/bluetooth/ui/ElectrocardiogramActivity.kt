package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.measurement.EcgUtil
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.MovingAverage
import com.dhealth.bluetooth.util.measurement.RxBus
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_electrocardiogram.*
import org.koin.android.ext.android.inject

class ElectrocardiogramActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val adapter: BleDataRVAdapter by inject()
    private val compositeDisposable: CompositeDisposable by inject()
    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable
    private val movingAverage = MovingAverage(5)

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        setSupportActionBar(toolbar)

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

        movingAverage.reset()
        doMeasurement(false)
    }

    override fun setLayout(): Int {
        return R.layout.activity_electrocardiogram
    }

    override fun onDestroy() {
        MeasurementUtil.commandStop(compositeDisposable, connection)
        connectionDisposable.dispose()
        super.onDestroy()
    }

    private fun doMeasurement(isDefault: Boolean){
        if (isDefault){
            EcgUtil.commandSendDefaultRegisterValues(compositeDisposable, connection)
        }
        else {
            EcgUtil.commandCreateGetRegister(compositeDisposable, connection)
        }

        EcgUtil.commandReadEcg(compositeDisposable, connection)
        EcgUtil.commandGetEcg(compositeDisposable, connection, isDefault, movingAverage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ecg_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_insert -> {
                toast("Invert ECG")
            }
        }
        return true
    }
}
