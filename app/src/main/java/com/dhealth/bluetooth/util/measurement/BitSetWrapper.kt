package com.dhealth.bluetooth.util.measurement

import java.util.*
import kotlin.jvm.internal.Intrinsics

class BitSetWrapper(private val bitSet: BitSet,
                    private var index: Int) {

    fun nextInt(value: Int): Int {
        val result = getInt(index, value)
        index += value
        return result
    }

    fun nextSignedInt(value: Int): Int {
        val result = if (bitSet[this.index + value]) {
            var i3: Int = this.index
            val i4: Int = i3 + value
            while (i3 < i4) {
                if (bitSet[i3]) {
                    bitSet.clear(i3)
                } else {
                    bitSet.set(i3)
                }
                i3++
            }
            -(nextInt(value) + 1)
        } else {
            nextInt(value)
        }
        this.index++
        return result
    }

    private fun getInt(value1: Int, value2: Int): Int {
        val longArray = bitSet[value1, value2 + value1].toLongArray()
        Intrinsics.checkExpressionValueIsNotNull(longArray, "longArray")
        return if (longArray.isEmpty()) 0 else longArray[0].toInt()
    }
}