package com.dhealth.bluetooth.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhealth.bluetooth.data.constant.AppConstants

@Entity(tableName = AppConstants.HEART_RATE)
data class Hrm(
    @ColumnInfo(name = "ch1") val green1Count: Int,
    @ColumnInfo(name = "ch2") val green2Count: Int,
    @ColumnInfo(name = "acceleration_x") val accelerationX: Float,
    @ColumnInfo(name = "acceleration_y") val accelerationY: Float,
    @ColumnInfo(name = "acceleration_z") val accelerationZ: Float,
    @ColumnInfo(name = "heart_rate") val heartRate: Int,
    @ColumnInfo(name = "confidence") val confidence: Int,
    @ColumnInfo(name = "spo2") val spo2: Float,
    @ColumnInfo(name = "activity") val activity: String,
    @PrimaryKey @ColumnInfo(name = "id") val id: Long){

    override fun toString(): String {
        return "channel 1: $green1Count\n" +
                "channel 2: $green2Count\n" +
                "accelerationX: $accelerationX\n" +
                "accelerationY: $accelerationY\n" +
                "accelerationZ: $accelerationZ\n" +
                "heart rate: $heartRate\n" +
                "heart rate confidence: $confidence\n" +
                "spo2: $spo2\n" +
                "activity: $activity\n"
    }
}