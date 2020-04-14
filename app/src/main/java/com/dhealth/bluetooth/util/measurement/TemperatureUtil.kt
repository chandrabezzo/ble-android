package com.dhealth.bluetooth.util.measurement

import android.icu.util.Measure
import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.Temperature
import com.parse.ParseObject
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object TemperatureUtil {
    fun mapper(values: ByteArray): Temperature {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        wrapper.nextInt(8)
        val celcius = wrapper.nextSignedInt(15).toFloat() / 100.toFloat()
        return Temperature(celcius, temperatureToFahrenheit(celcius), MeasurementUtil.getEpoch())
    }

    fun temperatureToCelcius(value: Float): Float {
        return (value - 32.toFloat()) / 1.8f
    }

    fun temperatureToFahrenheit(value: Float): Float {
        return value * 1.8f + 32.toFloat()
    }

    fun temperatureToJson(temperatures: MutableList<Temperature>): JSONArray {
        val jsonArray = JSONArray()

        for(temperature in temperatures){
            val json = JSONObject()
            json.put("ts", temperature.id)
            json.put("celcius", temperature.inCelcius)
            json.put("fahrenheit", temperature.inFahrenheit)

            jsonArray.put(json)
        }

        return jsonArray
    }

    fun temperatureToParseObject(temperature: Temperature): ParseObject {
        val parseObject = ParseObject("Temperature")
        parseObject.put("ts", temperature.id)
        parseObject.put("celcius", temperature.inCelcius)
        parseObject.put("fahrenheit", temperature.inFahrenheit)
        return parseObject
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
                callback.originalData(data)
                callback.temperature(MeasurementUtil.getEpoch(), temp.inCelcius, temp.inFahrenheit)
            }, {
                callback.onError(it)
            })
    }
}

interface TemperatureCallback {
    fun originalData(values: ByteArray)

    fun temperature(id: Long, celcius: Float, fahrenheit: Float)

    fun onError(error: Throwable)
}