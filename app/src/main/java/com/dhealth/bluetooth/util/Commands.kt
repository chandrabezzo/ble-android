package com.dhealth.bluetooth.util

import java.util.*

object Commands {
    const val ecgInvert = "set_cfg ecg invert"
    const val getDeviceInfo = "get_device_info"
    const val getRegister = "get_reg"
    const val read = "read"
    const val readEcg = "read ecg 2"
    const val readHrm = "read ppg 0"
    const val readSpo2 = "read ppg 1"
    const val readTemp = "read temp 0"
    const val setConfiguration = "set_cfg"
    const val setReg = "set_reg"
    const val setStreamTypeToBinary = "set_cfg stream bin"
    const val stop = "stop"

    fun createTempSampleIntervalCommand(interval: Int): String {
        val sb = StringBuilder()
        sb.append("set_cfg temp sr ")
        sb.append(interval)
        return sb.toString()
    }

    fun createSetRegisterCommand(str: String, i: Int, i2: Int): String {
        val sb = StringBuilder()
        sb.append("set_reg ")
        sb.append(str)
        sb.append(' ')
        sb.append(i.toString())
        sb.append(' ')
        sb.append(i2.toString())
        return sb.toString()
    }

    fun createGetRegisterCommand(str: String, i: Int): String {
        val sb = StringBuilder()
        sb.append("get_reg ")
        sb.append(str)
        sb.append(' ')
        sb.append(i.toString())
        return sb.toString()
    }

    fun createSetTimeCommand(): String {
        val calendar = Calendar.getInstance()
        val j = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60
            + calendar.get(Calendar.MINUTE) * 60
            + calendar.get(Calendar.SECOND)

        val sb = java.lang.StringBuilder()
        sb.append("set_cfg lcd time ")
        sb.append(j)
        return sb.toString()
    }

    fun createSetSpo2AlgorithmMode(z: Boolean): String {
        val sb = StringBuffer()
        sb.append("set_cfg spo2 algomode ")
        sb.append(if(z) "1" else "0")
        return sb.toString()
    }

    fun createLogToFlashCommand(z: Boolean): String {
        val sb = StringBuffer()
        sb.append("set_cfg flash log ")
        sb.append(if(z) "1" else "0")
        return sb.toString()
    }

    fun createMinConfidenceLevelCommand(i: Int): String {
        val sb = StringBuilder()
        sb.append("set_cfg whrm conf_level ")
        sb.append(i)
        return sb.toString()
    }

    fun createHrExpireDurationCommand(i: Int): String {
        val sb = StringBuilder()
        sb.append("set_cfg whrm hr_expiration ")
        sb.append(i)
        return sb.toString()
    }
}