package com.dhealth.bluetooth.data.model

import com.dhealth.bluetooth.util.measurement.HrmUtil

data class Hrm(val sampleCount: Int, val green1Count: Int, val green2Count: Int,
               val accelerationX: Float, val accelerationY: Float, val accelerationZ: Float,
               val heartRate: Int, val heartRateConfidence: Int, val spo2: Float, val activityCode: Int){

    override fun toString(): String {
        return "sample count: $sampleCount\n" +
                "channel 1: $green1Count\n" +
                "channel 2: $green2Count\n" +
                "accelerationX: $accelerationX\n" +
                "accelerationY: $accelerationY\n" +
                "accelerationZ: $accelerationZ\n" +
                "heart rate: $heartRate\n" +
                "heart rate confidence: $heartRateConfidence\n" +
                "spo2: $spo2\n" +
                "activity: ${HrmUtil.hrmActivity(activityCode)}\n"
    }
}