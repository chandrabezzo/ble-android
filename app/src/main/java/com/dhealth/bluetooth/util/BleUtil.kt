package com.dhealth.bluetooth.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log

object BleUtil {
    fun logGattStatus(status: Int){
        when(status){
            BluetoothGatt.GATT_SUCCESS -> {
                Log.i("GATT STATUS", "A GATT operation completed successfully")
            }
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                Log.i("GATT STATUS", "GATT read operation is not permitted")
            }
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                Log.i("GATT STATUS", "GATT write operation is not permitted")
            }
            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                Log.i("GATT STATUS", "Insufficient authentication for a given operation")
            }
            BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> {
                Log.i("GATT STATUS", "The given request is not supported")
            }
            BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                Log.i("GATT STATUS", "Insufficient encryption for a given operation")
            }
            BluetoothGatt.GATT_INVALID_OFFSET -> {
                Log.i("GATT STATUS", "A read or write operation was requested with an invalid offset")
            }
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                Log.i("GATT STATUS", "A write operation exceeds the maximum length of the attribute")
            }
            BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                Log.i("GATT STATUS", "A remote device connection is congested.")
            }
            BluetoothGatt.GATT_FAILURE -> {
                Log.i("GATT STATUS", "A GATT operation failed, errors other than the above")
            }
        }
    }

    fun logGattState(state: Int){
        when(state){
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.i("GATT STATE", "The profile is in disconnected state")
            }
            BluetoothProfile.STATE_CONNECTING -> {
                Log.i("GATT_STATE", "The profile is in connecting state")
            }
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i("GATT_STATE", "The profile is in connected state")
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                Log.i("GATT_STATE", "The profile is in disconnecting state")
            }
        }
    }
}