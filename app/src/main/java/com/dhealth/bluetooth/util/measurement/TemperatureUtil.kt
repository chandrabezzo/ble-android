package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.Temperature
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.util.*

object TemperatureUtil {
    fun mapper(values: ByteArray): Temperature {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        return Temperature(wrapper.nextInt(8), wrapper.nextSignedInt(15).toFloat() / 100.toFloat())
    }

    fun temperatureToCelcius(value: Float): Float {
        return (value - 32.toFloat()) / 1.8f
    }

    fun temperatureToFahrenheit(value: Float): Float {
        return value * 1.8f + 32.toFloat()
    }

    fun commandInterval(connection: Observable<RxBleConnection>, interval: Int): Disposable {
        val commandsInterval =
            MeasurementUtil.sendCommand(
                Commands.createTempSampleIntervalCommand(
                    interval
                )
            )
        return connection.flatMap {
            commandsInterval.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Interval", it?.contentToString())
        }, {
            Log.e("Error Interval", it.localizedMessage)
        })
    }

    fun commandReadTemp(connection: Observable<RxBleConnection>): Disposable {
        val commandsReadTemp =
            MeasurementUtil.sendCommand(
                Commands.readTemp
            )

        return connection.flatMap {
            commandsReadTemp.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read Temp", it?.contentToString())
        }, {
            Log.e("Error Read Temp", it.localizedMessage)
        })
    }

    fun commandGetTemperature(connection: Observable<RxBleConnection>,
                              callback: TemperatureCallback): Disposable {
        return connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                val temp = mapper(data)
                val fahrenheit = temperatureToFahrenheit(temp.temperature)
                callback.originalData(data)
                callback.temperatureInCelcius(temp.temperature)
                callback.temperatureInFahrenheit(fahrenheit)
            }, {
                callback.onError(it)
            })
    }
}

interface TemperatureCallback {
    fun originalData(values: ByteArray)

    fun temperatureInCelcius(value: Float)

    fun temperatureInFahrenheit(value: Float)

    fun onError(error: Throwable)
}