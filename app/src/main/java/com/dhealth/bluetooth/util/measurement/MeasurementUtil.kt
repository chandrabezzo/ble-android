package com.dhealth.bluetooth.util.measurement

import android.util.Log
import com.dhealth.bluetooth.data.constant.Maxim
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.nio.charset.Charset
import java.text.DecimalFormat

object MeasurementUtil {

    fun decimalFormat(value: Float): String {
        return DecimalFormat("#.00").format(value)
    }

    fun commandGetDeviceInfo(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandsGetDeviceInfo =
            sendCommand(
                Commands.getDeviceInfo
            )
        compositeDisposable.add(connection.flatMap {
            commandsGetDeviceInfo.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    fun commandCreateSetTime(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandsCreateSetTime =
            sendCommand(
                Commands.createSetTimeCommand()
            )
        compositeDisposable.add(connection.flatMap {
            commandsCreateSetTime.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    fun commandSetStreamTypeToBin(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandsSetStreamTypeToBin =
            sendCommand(
                Commands.setStreamTypeToBinary
            )
        compositeDisposable.add(connection.flatMap {
            commandsSetStreamTypeToBin.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Stream Type 2 Bin", it?.contentToString())
        }, {
            Log.e("Error Stream Type 2 Bin", it.localizedMessage)
        }))
    }

    fun commandStop(compositeDisposable: CompositeDisposable, connection: Observable<RxBleConnection>){
        val commandStop =
            sendCommand(
                Commands.stop
            )
        compositeDisposable.add(connection.flatMap {
            commandStop.toObservable().flatMap { data ->
                it.writeCharacteristic(Maxim.rawDataCharacteristic, data).toObservable()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.i("Data Create Set Time", it?.contentToString())
        }, {
            Log.e("Error Create Set Time", it.localizedMessage)
        }))
    }

    fun sendCommand(str: String): ArrayList<ByteArray> {
        var bArr: ByteArray?
        val sb = StringBuilder()
        sb.append(str)
        sb.append("\n")
        val chunked = sb.chunked(16)
        val arrayList = ArrayList<ByteArray>()
        for (str2 in chunked) {
            val length = 16 - str2.length
            when {
                length == 0 -> {
                    val charset: Charset = Charsets.UTF_8
                    bArr = str2.toByteArray(charset)
                }
                length >= 0 -> {
                    val bArr2 = ByteArray(16)
                    val charSequence: CharSequence = str2
                    var i = 0
                    var i2 = 0
                    while (i < charSequence.length) {
                        val i3 = i2 + 1
                        bArr2[i2] = charSequence[i].toByte()
                        i++
                        i2 = i3
                    }
                    for (length2 in str2.length..15) {
                        bArr2[length2] = 0
                    }
                    bArr = bArr2
                }
                else -> {
                    val sb2 = StringBuilder()
                    sb2.append("String is bigger than ")
                    sb2.append(16)
                    sb2.append(" bytes")
                    throw IllegalArgumentException(sb2.toString())
                }
            }
            arrayList.add(bArr)
        }

        return arrayList
    }
}