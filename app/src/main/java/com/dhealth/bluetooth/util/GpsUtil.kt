package com.dhealth.bluetooth.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog


class GpsUtil(private val context: Context) {

    fun checkStatusGPS(activity: Activity) {
        val manager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setTitle("Pengaturan GPS")
            builder.setMessage("Harap nyalakan GPS dengan mode akurasi tinggi.")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val intentLocationSetting =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(intentLocationSetting)
                }
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    activity.finish()
                }
            val alertDialog: AlertDialog = builder.create()
            if (!alertDialog.isShowing) {
                alertDialog.show()
            }
        }
    }

    fun isActive(activity: Activity): Boolean {
        val manager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun isMockLocationOn(location: Location): Boolean {
        return location.isFromMockProvider
    }
}