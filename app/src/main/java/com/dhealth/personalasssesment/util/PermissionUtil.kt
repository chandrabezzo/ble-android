package com.dhealth.personalasssesment.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dhealth.personalasssesment.R

object PermissionUtil {
    const val MY_PERMISSIONS_FINE_LOCATION = 129
    const val MY_PERMISSIONS_COARSE_LOCATION = 129

    fun requestFineLocationPermission(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                            String.format(context.getString(R.string.content_permission), "lokasi"),
                            arrayOf(permission), MY_PERMISSIONS_FINE_LOCATION)
                } else {
                    requestPermission(context, arrayOf(permission), MY_PERMISSIONS_FINE_LOCATION)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun requestCoarseLocationPermission(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                            String.format(context.getString(R.string.content_permission), "lokasi"),
                            arrayOf(permission), MY_PERMISSIONS_COARSE_LOCATION)
                } else {
                    requestPermission(context, arrayOf(permission), MY_PERMISSIONS_COARSE_LOCATION)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    private fun showAlertDialog(context: Context, title: String, message: String, permissions: Array<String>, flag: Int) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(message)
        alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which ->
            ActivityCompat.requestPermissions(context as Activity, permissions, flag)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun isNotGranted(context: Context, manifestPermission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, manifestPermission) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(context: Activity, permissions: Array<String>, flag: Int) {
        ActivityCompat.requestPermissions(context, permissions, flag)
    }
}