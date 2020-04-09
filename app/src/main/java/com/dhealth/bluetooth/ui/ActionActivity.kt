package com.dhealth.bluetooth.ui

import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.ui.measurement.MeasurementResultActivity
import com.dhealth.bluetooth.util.PermissionUtil
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_action.*
import org.koin.android.ext.android.inject

class ActionActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val compositeDisposable: CompositeDisposable by inject()
    private val bleClient: RxBleClient by inject()
    private lateinit var connection: Observable<RxBleConnection>

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connection = bleClient.getBleDevice(bleDevice?.device?.address!!)
            .establishConnection(true).compose(ReplayingShare.instance())

        MeasurementUtil.commandGetDeviceInfo(compositeDisposable, connection)
        MeasurementUtil.commandCreateSetTime(compositeDisposable, connection)
        MeasurementUtil.commandSetStreamTypeToBin(compositeDisposable, connection)

        cv_electrocardiogram.setOnClickListener {
            if(isPermissionGranted()){
                RxBus.publish(connection)
                launchActivity<ElectrocardiogramActivity> {
                    putExtra(Extras.BLE_DEVICE, bleDevice)
                }
            }
        }

        cv_optical_hrm.setOnClickListener {
            if(isPermissionGranted()){
                RxBus.publish(connection)
                launchActivity<OpticalHrmActivity> {
                    putExtra(Extras.BLE_DEVICE, bleDevice)
                }
            }
        }

        cv_temperature.setOnClickListener {
            if(isPermissionGranted()){
                RxBus.publish(connection)
                launchActivity<TemperatureActivity> {
                    putExtra(Extras.BLE_DEVICE, bleDevice)
                }
            }
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_action
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun isPermissionGranted(): Boolean {
        return PermissionUtil.requestWriteStorage(this) and PermissionUtil.requestReadStorage(this)
    }
}
