package com.dhealth.bluetooth.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhealth.bluetooth.data.constant.AppConstants
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = AppConstants.HEART_RATE)
data class Hrm(
    @SerializedName("ch1") @Expose @ColumnInfo(name = "ch1") val green1Count: Int,
    @SerializedName("ch2") @Expose @ColumnInfo(name = "ch2") val green2Count: Int,
    @SerializedName("acceleration_x") @Expose @ColumnInfo(name = "acceleration_x") val accelerationX: Float,
    @SerializedName("acceleration_y") @Expose @ColumnInfo(name = "acceleration_y") val accelerationY: Float,
    @SerializedName("acceleration_z") @Expose @ColumnInfo(name = "acceleration_z") val accelerationZ: Float,
    @SerializedName("heart_rate") @Expose @ColumnInfo(name = "heart_rate") val heartRate: Int,
    @SerializedName("confidence") @Expose @ColumnInfo(name = "confidence") val confidence: Int,
    @SerializedName("spo2") @Expose @ColumnInfo(name = "spo2") val spo2: Float,
    @SerializedName("activity") @Expose @ColumnInfo(name = "activity") val activity: String,
    @SerializedName("ts") @Expose @PrimaryKey @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "has_sync") var hasSync: Int = 0){

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