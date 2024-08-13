package com.ilya.MeetingMap.MENU.Server_API

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.http.Path

interface Became_Participant {

    @POST("/became/participant/{uid}/{key}/{id}")
    suspend fun Became_Participant(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Path("id") id: String
    ): Response<ResponseBody>  // Возвращаем Response<ResponseBody>
}

suspend fun Became_Participant_fun(
    uid: String,
    key: String,
    id: String
) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .build()  // Не добавляем конвертер Gson, если он не нужен

    val apiService = retrofit.create(Became_Participant::class.java)

    try {
        val response = apiService.Became_Participant(uid, key, id)
        if (response.isSuccessful) {
            Log.d("PushDataJoin", "Data successfully pushed to server")
            val responseBody = response.body()?.string()  // Получаем ответ в виде строки
            Log.d("PushDataJoin", "Response body: $responseBody")
        } else {
            Log.e("PushDataJoin", "Failed to push data to server. Error code: ${response.code()}")
            Log.e("PushDataJoin", "Error body: ${response.errorBody()?.string()}")
        }
    } catch (e: Exception) {
        Log.e("PushDataJoin", "Request failed with exception: ${e.message}")
    }
}
