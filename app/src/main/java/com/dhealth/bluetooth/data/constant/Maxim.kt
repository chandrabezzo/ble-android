package com.dhealth.bluetooth.data.constant

import java.util.*

object Maxim {
    val descriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    val service = UUID.fromString("00001523-1212-efde-1523-785feabcd123")
    val dataCharacteristic = UUID.fromString("00001011-1212-efde-1523-785feabcd123")
    val writeCharacteristic = UUID.fromString("00001022-1212-efde-1523-785feabcd123")
    val rawDataCharacteristic = UUID.fromString("00001027-1212-efde-1523-785feabcd123")
}