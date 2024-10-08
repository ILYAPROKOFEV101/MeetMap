package com.ilya.MeetingMap.Map.Server_API


import MapMarker

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GetMark {
    @GET("/get/public/mark/{uid}/{lat}/{lon}")
    suspend fun getMarker(
        @Path("uid") uid: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): List<MapMarker>
}



suspend fun getMarker(uid: String, latLng: LatLng): List<MapMarker> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(GetMark::class.java)

    return withContext(Dispatchers.IO) {
        try {
            val markerData = apiService.getMarker(uid, latLng.latitude, latLng.longitude)

            markerData
        } catch (e: Exception) {

            throw e
        }
    }
}

