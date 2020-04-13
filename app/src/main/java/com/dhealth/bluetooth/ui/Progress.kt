package com.dhealth.bluetooth.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.dhealth.bluetooth.R
import kotlinx.android.synthetic.main.dialog_progress.view.*

object Progress {
    fun dialog(context: Context, message: String): Dialog {
        val dialog = Dialog(context)
        val inflate = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        inflate.tv_message_progress.text = message
        dialog.setContentView(inflate)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}