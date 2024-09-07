package com.ilya.MeetingMap.MENU.Server_API

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path



// Создаем OkHttpClient один раз в приложении
val client: OkHttpClient by lazy {
    OkHttpClient.Builder().build()
}

suspend fun deleteParticipantMarker(uid: String, key: String, id: String): Boolean {
    Log.d("DELETE_MarkerData", "Deleting marker with id: $id, key: $key, uid: $uid")

    val url = "https://meetmap.up.railway.app/delete/participantmark/$uid/$key/$id"
    val request = Request.Builder()
        .url(url)
        .delete()
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("DELETE_MarkerData", "Marker successfully deleted.")
                true
            } else {
                Log.d("DELETE_MarkerData", "Failed to delete marker: ${response.code} ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("DELETE_MarkerData", "Error deleting marker", e)
            false
        }
    }
}
