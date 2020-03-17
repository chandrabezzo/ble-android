package com.dhealth.bluetooth.util

import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class BleService(private var bluetoothGatt: BluetoothGatt, private var uuid: UUID) {
    companion object {
        private const val disconnected = 0
        private const val connecting = 1
        private const val connected = 2

        const val gattConnected = "GattConnected"
        const val gattDisconnected = "GattDisconnected"
        const val gattServiceDiscovered = "GattServiceDiscovered"
        const val dataAvailable = "DataAvailable"
        const val extraData = "ExtraData"
    }

    private var connectionState = disconnected

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val intentAction: String
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = gattConnected
                    connectionState = connected
                    broadcastUpdate(intentAction)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = gattDisconnected
                    connectionState = disconnected
                    broadcastUpdate(intentAction)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(gattServiceDiscovered)
            }
            else {
                Log.w("BleService", "onServiceDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                characteristic?.let { broadcastUpdate(dataAvailable, it) }
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
//        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            uuid -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        Log.d("BleService", "Data format UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        Log.d("BleService", "Data format UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.d("BleService", String.format("Data : %d", heartRate))
                intent.putExtra(extraData, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    intent.putExtra(extraData, "$data\n$hexString")
                }
            }

        }
//        sendBroadcast(intent)
    }
}