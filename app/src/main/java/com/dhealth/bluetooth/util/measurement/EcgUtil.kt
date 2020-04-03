package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.constant.Sensors
import com.dhealth.bluetooth.data.model.Ecg
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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
            val i2 = next1 + i
            val next4 = wrapper.nextInt(3)
            val ecg = Ecg(
                i2,
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

    fun commandGetEcg(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>,
                      isDefault: Boolean, movingAverage: MovingAverage){
        compositeDisposable.add(connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                Log.i("Data ECG", data.contentToString())
                val ecgs = mapper(data, isDefault)
                for(ecg in ecgs){
                    if(ecg?.currentRToRBpm != 0){
                        ecg?.currentRToRBpm?.toFloat()?.let { movingAverage.add(it) }
                    }
                    ecg?.averageRToRBpm = movingAverage.getAverage()
                    Log.i("ECG", ecg.toString())
                }
            }, {
                Log.e("Error ECG", it.localizedMessage)
            }))
    }

    fun commandSendDefaultRegisterValues(compositeDisposable: CompositeDisposable,
                                         connection: Observable<RxBleConnection>){
        for(entry in Ecg.defaults.entries){
            val key = entry.key
            val value = entry.value

            val commandsSendRegister = MeasurementUtil.sendCommand(
                Commands.createSetRegisterCommand(Sensors.ECG, key, value)
            )

            compositeDisposable.add(connection.flatMap {
                commandsSendRegister.toObservable().flatMap { data ->
                    it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                Log.i("Send Register Values", it?.contentToString())
            }, {
                Log.e("Error Send Register", it.localizedMessage)
            }))
        }
    }

    fun commandCreateGetRegister(compositeDisposable: CompositeDisposable,
                                 connection: Observable<RxBleConnection>){
        val commandsCreateGetRegister =
            MeasurementUtil.sendCommand(
                Commands.createGetRegisterCommand(Sensors.ECG, Ecg.ecgGain.address)
            )

        compositeDisposable.add(connection.flatMap {
            commandsCreateGetRegister.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Get Register", it?.contentToString())
        }, {
            Log.e("Error Get Register", it.localizedMessage)
        }))
    }

    fun commandEcgInvert(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandsEcgInvert =
            MeasurementUtil.sendCommand(
                Commands.ecgInvert
            )

        compositeDisposable.add(connection.flatMap {
            commandsEcgInvert.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Ecg", it?.contentToString())
        }, {
            Log.e("Error Read Ecg", it.localizedMessage)
        }))
    }

    fun commandReadEcg(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandsReadEcg =
            MeasurementUtil.sendCommand(
                Commands.readEcg
            )

        compositeDisposable.add(connection.flatMap {
            commandsReadEcg.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Ecg", it?.contentToString())
        }, {
            Log.e("Error Read Ecg", it.localizedMessage)
        }))
    }
}