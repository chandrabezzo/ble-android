package com.dhealth.bluetooth.data.repository

import com.bezzo.core.data.session.SessionHelper
import com.dhealth.bluetooth.data.constant.AppConstants
import java.util.*

class MeasurementRepository(private val session: SessionHelper) {
    fun selectDevice(address: String){
        session.addSession(AppConstants.SELECTED_DEVICE, address)
    }

    fun selectedDevice(): String {
        return session.getSession(AppConstants.SELECTED_DEVICE, "-")
    }

    fun deviceConnect(isConnect: Boolean){
        session.addSession(AppConstants.IS_CONNECT, isConnect)
    }

    fun isDeviceConnect(): Boolean {
        return session.getSession(AppConstants.IS_CONNECT, false) ?: false
    }

    fun measurementType(type: Int){
        session.addSession(AppConstants.MEASUREMENT_TYPE, type)
    }

    fun measurementType(): Int {
        return session.getSession(AppConstants.MEASUREMENT_TYPE, 1.toInt()) ?: 1
    }

    fun isChecking(checking: Boolean){
        session.addSession(AppConstants.IS_CHECKING, checking)
    }

    fun isChecking(): Boolean {
        return session.getSession(AppConstants.IS_CHECKING, false) ?: false
    }

    fun workerId(id: UUID){
        session.addSession(AppConstants.WORKER_MOST_SIG_BIT, id.mostSignificantBits)
        session.addSession(AppConstants.WORKER_LEAST_SIG_BIT, id.leastSignificantBits)
    }

    fun workerId(): UUID {
        val mostSigBit = session.getSession(AppConstants.WORKER_MOST_SIG_BIT, 0L)
        val leastSigBit = session.getSession(AppConstants.WORKER_LEAST_SIG_BIT, 0L)
        return UUID(mostSigBit ?: 0L, leastSigBit ?: 0L)
    }
}