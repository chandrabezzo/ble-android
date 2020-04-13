package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.dhealth.bluetooth.data.model.Hrm
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object HrmUtil {

    fun mapper(values: ByteArray): Hrm {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        wrapper.nextInt(16) // sample count
        return Hrm(
            wrapper.nextInt(20),
            wrapper.nextInt(20),
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextSignedInt(13).toFloat() / 1000.0f,
            wrapper.nextInt(12),
            wrapper.nextInt(8),
            wrapper.nextInt(11).toFloat() / 10.0f,
            hrmActivity(wrapper.nextInt(8)),
            System.currentTimeMillis()
        )
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

    fun hrmToJson(hrms: MutableList<Hrm>): JSONArray {
        val jsonArray = JSONArray()

        for(hrm in hrms){
            val json = JSONObject()
            json.put("id", hrm.id)
            json.put("acceleration_x", hrm.accelerationX)
            json.put("acceleration_y", hrm.accelerationY)
            json.put("acceleration_z", hrm.accelerationZ)
            json.put("ch1", hrm.green1Count)
            json.put("ch2", hrm.green2Count)
            json.put("activity", hrm.activity)
            json.put("confidence", hrm.confidence)
            json.put("heart_rate", hrm.heartRate)
            json.put("spo2", hrm.spo2)

            jsonArray.put(json)
        }

        return jsonArray
    }

    fun commandMinConfidenceLevel(connection: Observable<RxBleConnection>,
                                  confidenceLevel: Int) : Disposable {
        val minConfidenceLevel =
            MeasurementUtil.sendCommand(Commands.createMinConfidenceLevelCommand(confidenceLevel))
        return connection.flatMap {
            minConfidenceLevel.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Confidence Level", it?.contentToString())
        }, {
            Log.e("Error Confidence Level", it.localizedMessage)
        })
    }

    fun commandHrExpireDuration(connection: Observable<RxBleConnection>,
                                expireDuration: Int): Disposable {
        val hrmExpireDuration =
            MeasurementUtil.sendCommand(Commands.createHrExpireDurationCommand(expireDuration))
        return connection.flatMap {
            hrmExpireDuration.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Expire Duration", it?.contentToString())
        }, {
            Log.e("Error Expire Duration", it.localizedMessage)
        })
    }

    fun commandReadHrm(connection: Observable<RxBleConnection>): Disposable {
        val readHrm =
            MeasurementUtil.sendCommand(Commands.readHrm)
        return connection.flatMap {
            readHrm.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Read HRM", it?.contentToString())
        }, {
            Log.e("Error Read HRM", it.localizedMessage)
        })
    }

    fun commandGetHrm(connection: Observable<RxBleConnection>,
                      callback: HrmCallback): Disposable {
        return connection.flatMap { it.setupNotification(Maxim.dataCharacteristic) }
            .flatMap { observable -> observable }
            .filter {
                it[0] == 170.toByte()
            }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                val hrm = mapper(data)
                callback.originalData(data)
                callback.heartRateMonitor(hrm.id, hrm.green1Count, hrm.green2Count, hrm.heartRate,
                    hrm.confidence, hrm.activity, hrm.accelerationX,
                    hrm.accelerationY, hrm.accelerationZ, hrm.spo2)
            }, {
                Log.e("Error Get HRM", it.localizedMessage)
            })
    }
}

interface HrmCallback {
    fun originalData(values: ByteArray)

    fun heartRateMonitor(id: Long, channel1: Int, channel2: Int, heartRate: Int, confidence: Int,
                         activity: String, accelerationX: Float, accelerationY: Float,
                         accelerationZ: Float, spo2: Float)
}