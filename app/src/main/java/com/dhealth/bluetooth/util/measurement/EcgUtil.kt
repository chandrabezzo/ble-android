package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.constant.Sensors
import com.dhealth.bluetooth.data.model.Ecg
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.util.*

object EcgUtil {
    private fun mapper(values: ByteArray, isDefault: Boolean): Array<Ecg?> {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        val next1 = wrapper.nextInt(8)
        val next2 = wrapper.nextInt(14)
        val next3 = wrapper.nextInt(8)

        val ecgs = arrayOfNulls<Ecg>(4)
        var i = 0
        while (i < ecgs.size) {
            next1 + i // sample count
            val next4 = wrapper.nextInt(3)
            val ecg = Ecg(
                wrapper.nextSignedInt(17),
                wrapper.nextInt(3),
                next4,
                if (i == 0) next2 else 0,
                if (i == 0) next3 else 0,
                0.0f, 0.0f, 0.0f, 0,
                960, isDefault
            )
            ecgs[i] = ecg
            i++
        }

        return ecgs
    }

    fun commandGetEcg(connection: Observable<RxBleConnection>, isDefault: Boolean,
                      movingAverage: MovingAverage, callback: EcgCallback): Disposable {
        return connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                callback.originalData(data)
                val ecgs = mapper(data, isDefault)
                for(ecg in ecgs){
                    if(ecg?.currentRToRBpm != 0){
                        ecg?.currentRToRBpm?.toFloat()?.let { movingAverage.add(it) }
                    }

                    ecg?.averageRToRBpm = movingAverage.getAverage()
                    ecg?.let {
                        callback.ecgMonitor(it.id, it.ecg, it.eTag, it.pTag, it.rTor, it.currentRToRBpm,
                            it.ecgMv, it.filteredEcg, it.averageRToRBpm, it.counterToReport)
                    }
                }
            }, {
                Log.e("Error ECG", it.localizedMessage)
            })
    }

    fun commandSendDefaultRegisterValues(connection: Observable<RxBleConnection>, key: Int, value: Int)
            : Disposable {
        val commandsSendRegister = MeasurementUtil.sendCommand(
            Commands.createSetRegisterCommand(Sensors.ECG, key, value)
        )

        return connection.flatMap {
            commandsSendRegister.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Send Register Values", it?.contentToString())
        }, {
            Log.e("Error Send Register", it.localizedMessage)
        })
    }

    fun commandCreateGetRegister(connection: Observable<RxBleConnection>): Disposable {
        val commandsCreateGetRegister =
            MeasurementUtil.sendCommand(
                Commands.createGetRegisterCommand(Sensors.ECG, Ecg.ecgGain.address)
            )

        return connection.flatMap {
            commandsCreateGetRegister.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Get Register", it?.contentToString())
        }, {
            Log.e("Error Get Register", it.localizedMessage)
        })
    }

    fun commandEcgInvert(connection: Observable<RxBleConnection>): Disposable {
        val commandsEcgInvert =
            MeasurementUtil.sendCommand(
                Commands.ecgInvert
            )

        return connection.flatMap {
            commandsEcgInvert.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Ecg", it?.contentToString())
        }, {
            Log.e("Error Read Ecg", it.localizedMessage)
        })
    }

    fun commandReadEcg(connection: Observable<RxBleConnection>): Disposable {
        val commandsReadEcg =
            MeasurementUtil.sendCommand(
                Commands.readEcg
            )

        return connection.flatMap {
            commandsReadEcg.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Ecg", it?.contentToString())
        }, {
            Log.e("Error Read Ecg", it.localizedMessage)
        })
    }
}

interface EcgCallback {
    fun originalData(values: ByteArray)

    fun ecgMonitor(id: Long, ecg: Int, eTag: Int, pTag: Int, rTor: Int, currentRToRBpm: Int, ecgMv: Float,
        filteredEcg: Float, averageRToRBpm: Float, counterToReport: Float)
}