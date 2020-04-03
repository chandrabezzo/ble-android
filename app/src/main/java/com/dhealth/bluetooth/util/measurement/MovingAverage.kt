package com.dhealth.bluetooth.util.measurement

class MovingAverage(private val index: Int) {

    private val circularWindow = arrayOfNulls<Float>(index)
    private var windowItemCount = 0
    private var windowStartIndex = 0
    private var windowSum = 0.0f

    fun getAverage(): Float {
        val i: Int = this.windowItemCount
        return if (i != 0) {
            this.windowSum / i.toFloat()
        } else 0.0f
    }

    fun add(f: Float) {
        val i = windowItemCount
        val fArr: Array<Float?> = circularWindow
        if (i == fArr.size) {
            fArr[windowStartIndex]?.let { startIndex ->
                windowSum -= startIndex
            }
        } else {
            windowItemCount = i + 1
        }
        windowSum += f.toInt()
        val fArr2: Array<Float?> = circularWindow
        val i2 = windowStartIndex
        windowStartIndex = i2 + 1
        fArr2[i2] = f
        if (windowStartIndex == fArr2.size) {
            windowStartIndex = 0
        }
    }

    fun reset() {
        windowSum = 0.0f
        windowItemCount = 0
        windowStartIndex = 0
    }
}