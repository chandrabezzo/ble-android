package com.dhealth.bluetooth.util

import com.dhealth.bluetooth.data.model.Temperature
import java.util.*

object TempMapper {
    fun map(values: ByteArray): Temperature {
        val wrapper = BitSetWrapper(BitSet.valueOf(values), 8)
        return Temperature(wrapper.nextInt(8), wrapper.nextSignedInt(15).toFloat() / 100.toFloat())
    }
}