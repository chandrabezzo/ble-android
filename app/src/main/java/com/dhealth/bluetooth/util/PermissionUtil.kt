package com.dhealth.bluetooth.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dhealth.bluetooth.R

object PermissionUtil {
    private const val FINE_LOCATION = 1
    private const val COARSE_LOCATION = 2
    private const val READ_STORAGE = 3
    private const val WRITE_STORAGE = 4

    fun requestFineLocationPermission(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            return if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                        String.format(context.getString(R.string.content_permission), "lokasi"),
                        arrayOf(permission), FINE_LOCATION)
                } else {
                    requestPermission(context, arrayOf(permission), FINE_LOCATION)
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    fun requestCoarseLocationPermission(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            return if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                        String.format(context.getString(R.string.content_permission), "lokasi"),
                        arrayOf(permission), COARSE_LOCATION)
                } else {
                    requestPermission(context, arrayOf(permission), COARSE_LOCATION)
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    fun requestWriteStorage(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            return if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                        String.format(context.getString(R.string.content_permission), "penyimpanan file"),
                        arrayOf(permission), WRITE_STORAGE)
                } else {
                    requestPermission(context, arrayOf(permission), WRITE_STORAGE)
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    fun requestReadStorage(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            return if (isNotGranted(context, permission)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showAlertDialog(context, context.getString(R.string.title_permission),
                        String.format(context.getString(R.string.content_permission), "penyimpanan file"),
                        arrayOf(permission), READ_STORAGE)
                } else {
                    requestPermission(context, arrayOf(permission), READ_STORAGE)
                }
                false
            } else {
                true
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