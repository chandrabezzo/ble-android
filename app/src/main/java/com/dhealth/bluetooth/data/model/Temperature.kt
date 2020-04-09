package com.dhealth.bluetooth.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhealth.bluetooth.data.constant.AppConstants

@Entity(tableName = AppConstants.TEMPERATURE)
data class Temperature(
    @ColumnInfo(name = "in_celcius") val inCelcius: Float,
    @ColumnInfo(name = "in_fahrenheit") val inFahrenheit: Float,
    @PrimaryKey @ColumnInfo(name = "id") val id: Long
)