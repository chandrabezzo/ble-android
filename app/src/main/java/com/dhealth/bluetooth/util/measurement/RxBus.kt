package com.dhealth.bluetooth.util.measurement

import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

import io.reactivex.subjects.BehaviorSubject

object RxBus {
    private val behaviorSubject = BehaviorSubject.create<Observable<RxBleConnection>>()

    fun subscribe(action: Consumer<Observable<RxBleConnection>>): Disposable {
        return behaviorSubject.subscribe(action)
    }

    fun publish(message: Observable<RxBleConnection>) {
        behaviorSubject.onNext(message)
    }
}