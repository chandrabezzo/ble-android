package com.dhealth.bluetooth.util

sealed class ShareState

object Loading: ShareState()
object Saved : ShareState()