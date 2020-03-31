package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.Hrm
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.util.*

object HrmUtil {

    fun mapper(values: ByteArray): Hrm {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        return Hrm(
            wrapper.nextInt(16),
            wrapper.nextInt(20),
            wrapper.nextInt(20),
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextInt(12),
            wrapper.nextInt(8),
            wrapper.nextInt(11).toFloat() / 10.0f,
            wrapper.nextInt(8))
    }

    fun hrmActivity(activityCode: Int): String {
        return when(activityCode){
            0 -> "REST"
            1 -> "OTHER"
            2 -> "WALK"
            3 -> "RUN"
            4 -> "BIKE"
            5 -> "OTHER RHYTHMIC"
            else -> "--"
        }
    }

    fun commandMinConfidenceLevel(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>,
                                  confidenceLevel: Int){
        val minConfidenceLevel =
            MeasurementUtil.sendCommand(Commands.createMinConfidenceLevelCommand(confidenceLevel))
        compositeDisposable.add(connection.flatMap {
            minConfidenceLevel.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Confidence Level", it?.contentToString())
        }, {
            Log.e("Error Confidence Level", it.localizedMessage)
        }))
    }

    fun commandHrExpireDuration(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>,
                                expireDuration: Int){
        val hrmExpireDuration =
            MeasurementUtil.sendCommand(Commands.createHrExpireDurationCommand(expireDuration))
        compositeDisposable.add(connection.flatMap {
            hrmExpireDuration.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Expire Duration", it?.contentToString())
        }, {
            Log.e("Error Expire Duration", it.localizedMessage)
        }))
    }

    fun commandReadHrm(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val readHrm =
            MeasurementUtil.sendCommand(Commands.readHrm)
        compositeDisposable.add(connection.flatMap {
            readHrm.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read HRM", it?.contentToString())
        }, {
            Log.e("Error Read HRM", it.localizedMessage)
        }))
    }

    fun commandGetHrm(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        compositeDisposable.add(connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                val hrm = mapper(data)
                Log.i("Data Get HRM", data.contentToString())
                Log.i("Data HRM", hrm.toString())
            }, {
                Log.e("Error Get HRM", it.localizedMessage)
            }))
    }
}