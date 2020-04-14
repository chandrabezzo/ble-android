package com.dhealth.bluetooth.util

sealed class ShareState

object Prepare: ShareState()
object Saved : ShareState()