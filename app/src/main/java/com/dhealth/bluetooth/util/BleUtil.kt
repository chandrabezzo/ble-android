package com.dhealth.bluetooth.util

import android.bluetooth.*
import android.bluetooth.le.ScanRecord
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.reflect.Method

object BleUtil {
    fun gattStatus(status: Int): String {
        when(status){
            BluetoothGatt.GATT_SUCCESS -> {
                return "A GATT operation completed successfully"
            }
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                return "GATT read operation is not permitted"
            }
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                return "GATT write operation is not permitted"
            }
            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                return "Insufficient authentication for a given operation"
            }
            BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> {
                return "The given request is not supported"
            }
            BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                return "Insufficient encryption for a given operation"
            }
            BluetoothGatt.GATT_INVALID_OFFSET -> {
                return "A read or write operation was requested with an invalid offset"
            }
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                return "A write operation exceeds the maximum length of the attribute"
            }
            BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                return "A remote device connection is congested."
            }
            BluetoothGatt.GATT_FAILURE -> {
                return "A GATT operation failed, errors other than the above"
            }
            else -> {
                return "Undefined Gatt Status"
            }
        }
    }

    fun gattState(state: Int): String {
        when(state){
            BluetoothProfile.STATE_DISCONNECTED -> {
                return "The profile is in disconnected state"
            }
            BluetoothProfile.STATE_CONNECTING -> {
                return "The profile is in connecting state"
            }
            BluetoothProfile.STATE_CONNECTED -> {
                return "The profile is in connected state"
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                return "The profile is in disconnecting state"
            }
            else -> {
                return "Undefined Gatt State"
            }
        }
    }

    fun bondState(state: Int): String {
        when(state){
            BluetoothDevice.BOND_NONE -> {
                return "Indicates the remote device is not bonded (paired).\n" +
                        "There is no shared link key with the remote device, so communication\n" +
                        "(if it is allowed at all) will be unauthenticated and unencrypted."
            }
            BluetoothDevice.BOND_BONDING -> {
                return "Indicates bonding (pairing) is in progress with the remote device."
            }
            BluetoothDevice.BOND_BONDED -> {
                return "Indicates the remote device is bonded (paired).\n" +
                        "A shared link keys exists locally for the remote device, so\n" +
                        "communication can be authenticated and encrypted.\n" +
                        "Being bonded (paired) with a remote device does not necessarily\n" +
                        "mean the device is currently connected. It just means that the pending\n" +
                        "procedure was completed at some earlier time, and the link key is still\n" +
                        "stored locally, ready to use on the next connection.\n"
            }
            else -> {
                return "Undefined Bond State"
            }
        }
    }

    fun type(type: Int): String {
        return when(type){
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> {
                "Classic - BR/EDR devices"
            }
            BluetoothDevice.DEVICE_TYPE_LE -> {
                "Low Energy - LE-only"
            }
            BluetoothDevice.DEVICE_TYPE_DUAL -> {
                "Dual Mode - BR/EDR/LE"
            }
            else -> {
                "Unknown"
            }
        }
    }

    fun characteristicProperty(type: Int): String {
        return when(type){
            BluetoothGattCharacteristic.PROPERTY_BROADCAST -> {
                "Characteristic is broadcastable"
            }
            BluetoothGattCharacteristic.PROPERTY_READ -> {
                "Characteristic is readable"
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE -> {
                "Characteristic can be written without response"
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE -> {
                "Characteristic can be written"
            }
            BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                "Characteristic supports notification"
            }
            BluetoothGattCharacteristic.PROPERTY_INDICATE -> {
                "Characteristic supports indication"
            }
            BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE -> {
                "Characteristic supports write with signature"
            }
            BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS -> {
                "Characteristic has extended properties"
            }
            else -> {
                "Unknown Property"
            }
        }
    }

    fun characteristicPermission(type: Int): String {
        return when(type){
            BluetoothGattCharacteristic.PERMISSION_READ -> {
                "Read permission"
            }
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED -> {
                "Allow encrypted read operations"
            }
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM -> {
                "Allow reading with man-in-the-middle protection"
            }
            BluetoothGattCharacteristic.PERMISSION_WRITE -> {
                "Write permission"
            }
            BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED -> {
                "Allow encrypted writes"
            }
            BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM -> {
                "Allow encrypted writes with man-in-the-middle protection"
            }
            BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED -> {
                "Allow signed write operations"
            }
            BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM -> {
                "Allow signed write operations with man-in-the-middle protection"
            }
            else -> {
                "Unknown Permission"
            }
        }
    }

    fun serviceType(type: Int): String {
        return when(type){
            BluetoothGattService.SERVICE_TYPE_PRIMARY -> {
                "Primary service"
            }
            BluetoothGattService.SERVICE_TYPE_SECONDARY -> {
                "Secondary service (included by primary services)"
            }
            else -> {
                "Unknown Service Type"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun parseScanRecordFromBytes(bytes: ByteArray?): ScanRecord? {
        return try {
            val parseFromBytes: Method =
                ScanRecord::class.java.getMethod("parseFromBytes", ByteArray::class.java)
            parseFromBytes.invoke(null, bytes as Any?) as ScanRecord
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}