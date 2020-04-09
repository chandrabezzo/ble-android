package com.dhealth.bluetooth.util

import android.content.Context
import android.os.Environment
import com.dhealth.bluetooth.data.model.Temperature
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

object StorageUtil {
    const val temperatureName = "temperature.json"

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun temperatureToJson(temperatures: ArrayList<Temperature>): JSONArray {
        val jsonArray = JSONArray()

        for(temperature in temperatures){
            val json = JSONObject()
            json.put("time", Calendar.getInstance().timeInMillis)
            json.put("celcius", temperature.inCelcius)
            json.put("fahrenheit", temperature.inFahrenheit)

            jsonArray.put(json.toString())
        }

        return jsonArray
    }

    fun saveTemperature(context: Context, temperature: JSONArray){
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${Calendar.getInstance().timeInMillis} - $temperatureName")
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(temperature.toString())
        bufferedWriter.close()
    }

    fun readTemperature(context: Context): JSONObject {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), temperatureName)
        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null){
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        return JSONObject(stringBuilder.toString())
    }
}