package com.dhealth.bluetooth.data.model

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BleDevice(
    val device: BluetoothDevice,
    val rssi: Int,
    val scanRecord: ByteArray?
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleDevice

        if (device != other.device) return false
        if (rssi != other.rssi) return false
        if (scanRecord != null) {
            if (other.scanRecord == null) return false
            if (!scanRecord.contentEquals(other.scanRecord)) return false
        } else if (other.scanRecord != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + rssi
        result = 31 * result + (scanRecord?.contentHashCode() ?: 0)
        return result
    }
}