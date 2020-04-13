package com.dhealth.bluetooth.util

import android.content.Context
import android.os.Environment
import org.json.JSONArray
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object StorageUtil {
    private const val temperatureName = "temperature.json"
    private const val hrmName = "heart_rate_monitoring.json"
    private const val ecgName = "ecg_monitoring.json"
    private const val measurementsFolder = "measurements"

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun saveTemperature(context: Context, temperature: JSONArray){
        val file = File(context.getExternalFilesDir(measurementsFolder),
            temperatureName)
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(temperature.toString())
        bufferedWriter.close()
    }

    fun readTemperature(context: Context): File {
        return File(context.getExternalFilesDir(measurementsFolder), temperatureName)
    }

    fun saveHrm(context: Context, hrms: JSONArray){
        val file = File(context.getExternalFilesDir(measurementsFolder),
            hrmName)
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(hrms.toString())
        bufferedWriter.close()
    }

    fun readHrm(context: Context): File {
        return File(context.getExternalFilesDir(measurementsFolder), hrmName)
    }

    fun saveEcg(context: Context, ecgs: JSONArray){
        val file = File(context.getExternalFilesDir(measurementsFolder),
            ecgName)
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(ecgs.toString())
        bufferedWriter.close()
    }

    fun readEcg(context: Context): File {
        return File(context.getExternalFilesDir(measurementsFolder), ecgName)
    }
}