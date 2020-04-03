package com.dhealth.bluetooth.data.model

import java.util.*

class Ecg(
    val sampleCount: Int,
    val ecg: Int,
    val eTag: Int,
    val pTag: Int,
    val rTor: Int,
    val currentRToRBpm: Int,
    val ecgMv: Float,
    val filteredEcg: Float,
    var averageRToRBpm: Float,
    val counterToReport: Float,
    val timeStamp: Long = System.currentTimeMillis()
){
    companion object {
        val ecgGain = RegisterField(21, 2, 16)
        private val ecgGainValues = intArrayOf(20, 40, 80, 120)
        val defaults: SortedMap<Int, Int> = sortedMapOf(
            Pair(21, 8409088),
            Pair(16, 524288),
            Pair(18, 16384),
            Pair(29, 3515136),
            Pair(20, 0)
        )

        private fun extractValue(value: Int): Int {
            val value1 = value.ushr(ecgGain.bitShift)
            val value2 = (1.shl(ecgGain.bitWidth) - 1)
            return value1 and value2
        }

        private fun getDefaultEcgGain(): Int {
            val number = defaults[ecgGain.address]
            val extractValue = extractValue(number ?: 0)
            return ecgGainValues[3]
        }

        fun getEcgMv(i2: Int, i9: Int, f: Float, isDefault: Boolean): Float {
            val adcToMv = 0.0076293945f
            val gain = if(isDefault) getDefaultEcgGain() else ecgGain.address
            return if (i9 and 64 !== 0) {
                i2.toFloat() * adcToMv / gain
            } else {
                f
            }
        }
    }

    constructor(i: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, f: Float, f2: Float, f3: Float,
        i7: Int, i8: Int, isDefault: Boolean) : this(
        i, i2, i3, i4, i5, i6,
        getEcgMv(i2, i8, f, isDefault),
        if (i8 and 128 !== 0) 0.0f else f2,
        if (i8 and 256 !== 0) 0.0f else f3,
        (if (i8 and 512 !== 0) 0 else i7).toFloat()
    )

    override fun toString(): String {
        return "SampleCount: $sampleCount, ECG: $ecg, eTag: $eTag, pTag: $pTag, RToR: $rTor, " +
                "CurrentRToRBpm: $currentRToRBpm, EcgMV: $ecgMv, FilteredEcg: $filteredEcg, " +
                "AvgRToRBpm: $averageRToRBpm, CounterToReport: $counterToReport, TimeStamp: $timeStamp"
    }
}