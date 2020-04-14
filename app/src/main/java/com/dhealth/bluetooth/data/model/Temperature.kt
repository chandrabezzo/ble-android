package com.dhealth.bluetooth.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhealth.bluetooth.data.constant.AppConstants
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = AppConstants.TEMPERATURE)
data class Temperature(
    @SerializedName("in_celcius") @Expose @ColumnInfo(name = "in_celcius") val inCelcius: Float,
    @SerializedName("in_fahrenheit") @Expose @ColumnInfo(name = "in_fahrenheit") val inFahrenheit: Float,
    @SerializedName("ts") @Expose @PrimaryKey @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "has_sync") var hasSync: Int = 0
)