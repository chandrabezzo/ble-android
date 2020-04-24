package com.dhealth.bluetooth.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import com.bezzo.core.base.BaseViewModel
import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.data.constant.AppConstants
import com.dhealth.bluetooth.data.repository.MeasurementRepository
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.TemperatureCallback
import com.dhealth.bluetooth.util.measurement.TemperatureUtil
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

class MeasurementViewModel(private val compositeDisposable: CompositeDisposable,
                           private val repository: MeasurementRepository,
                           application: Application)
    : BaseViewModel(application) {

    private var tempIntervalDisposable: Disposable? = null
    private var readTempDisposable: Disposable? = null
    private var getTemperatureDisposable: Disposable? = null

    fun selectedDevice(device: BluetoothDevice){
        repository.selectDevice(device.address)
    }

    fun selectedDevice(): String {
        return repository.selectedDevice()
    }

    fun deviceConnect(isConnect: Boolean){
        repository.deviceConnect(isConnect)
    }

    fun isDeviceConnect(): Boolean {
        return repository.isDeviceConnect()
    }

    fun measurementType(type: Int){
        repository.measurementType(type)
    }

    fun measurementType(): Int {
        return repository.measurementType()
    }

    fun isChecking(checking: Boolean){
        repository.isChecking(checking)
    }

    fun isChecking(): Boolean {
        return repository.isChecking()
    }

    fun disposeComposite() {
        return compositeDisposable.dispose()
    }

    fun prepareDevice(connection: Observable<RxBleConnection>){
        compositeDisposable.add(MeasurementUtil.commandGetDeviceInfo(connection))
        compositeDisposable.add(MeasurementUtil.commandCreateSetTime(connection))
        compositeDisposable.add(MeasurementUtil.commandSetStreamTypeToBin(connection))
    }

    fun monitoringTemperature(connection: Observable<RxBleConnection>, callback: TemperatureCallback){
        tempIntervalDisposable = TemperatureUtil.commandInterval(connection, 1000)
        tempIntervalDisposable?.let { compositeDisposable.add(it) }

        readTempDisposable = TemperatureUtil.commandReadTemp(connection)
        readTempDisposable?.let { compositeDisposable.add(it) }

        getTemperatureDisposable = TemperatureUtil.commandGetTemperature(connection, callback)
        getTemperatureDisposable?.let { compositeDisposable.add(it) }
    }

    fun stopMonitoring(connection: Observable<RxBleConnection>){
        MeasurementUtil.commandStop(connection)
        tempIntervalDisposable?.dispose()
        readTempDisposable?.dispose()
        getTemperatureDisposable?.dispose()
    }

    fun workerId(id: UUID){
        repository.workerId(id)
    }

    fun workerId(): UUID {
        return repository.workerId()
    }
}