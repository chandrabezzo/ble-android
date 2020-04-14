package com.dhealth.bluetooth.data.network

import com.bezzo.core.data.network.Client
import com.dhealth.bluetooth.BuildConfig
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.model.Temperature
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class ApiService {
    fun local(): LocalService {
        return Client.crudClient(BuildConfig.BASE_URL).create(LocalService::class.java)
    }
}

interface LocalService {
    @GET(Endpoints.temperatures)
    fun getTemperatures(): Call<Temperature>

    @POST(Endpoints.temperatures)
    fun syncTemperature(@Body temprature: Temperature): Call<Temperature>

    @GET(Endpoints.heartRates)
    fun getHeartRates(): Call<Hrm>

    @POST(Endpoints.heartRates)
    fun syncHeartRate(@Body hrm: Hrm): Call<Hrm>

    @GET(Endpoints.ecgs)
    fun getEcgs(): Call<Ecg>

    @POST(Endpoints.ecgs)
    fun syncEcg(@Body ecg: Ecg): Call<Ecg>
}