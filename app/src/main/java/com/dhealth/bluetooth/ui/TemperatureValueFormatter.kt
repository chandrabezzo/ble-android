package com.dhealth.bluetooth.ui

import android.content.Context
import com.dhealth.bluetooth.R
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class TemperatureValueFormatter(private val context: Context,
                                private val isCelcius: Boolean)
    : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val tempUnit = if(isCelcius) context.getString(R.string.derajat_celcius)
            else context.getString(R.string.derajat_fahrenheit)
        return "$value$tempUnit"
    }
}