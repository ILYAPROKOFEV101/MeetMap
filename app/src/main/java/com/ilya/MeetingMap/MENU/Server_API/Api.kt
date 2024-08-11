package com.ilya.MeetingMap.MENU.Server_API

import MarkerData
import android.annotation.SuppressLint
import android.app.VoiceInteractor
import android.content.Context
import android.util.Log
import com.ilya.reaction.logik.PreferenceHelper.setUserKey
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException

interface Post_Mark {
    @POST("/mark/{uid}/{key}")
    fun postMarker(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Body request: MarkerData
    ): Call<Void>
}


fun postInvite(key: String, uid: String, markerData: MarkerData) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(Post_Mark::class.java)

    val call = apiService.postMarker(uid, key, markerData)
    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("PushDataJoin", "Data successfully pushed to server")
            } else {
                Log.e("PushDataJoin", "Failed to push data to server. Error code: ${response.code()}")
                Log.e("PushDataJoin", "Error body: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("PushDataJoin", "Failed to push data to server. Error message: ${t.message}")
        }
    })
}


