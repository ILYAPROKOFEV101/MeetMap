package com.ilya.MeetingMap.Map.Server_API

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.http.Path

interface AddFriendsService {
    @POST("/friendrequest/{uid}/{key}/{friend_key}")
    fun addFriends(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Path("friend_key") friendKey: String
    ): Call<Void> // Возвращаем Call<Void>
}

suspend fun addFriends(uid: String, key: String, friendKey: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .build()

    val apiService = retrofit.create(AddFriendsService::class.java)

    apiService.addFriends(uid, key, friendKey).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("AddFriendsService", "Data successfully pushed to server")
            } else {
                Log.e("AddFriendsService", "Error response: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("AddFriendsService", "Request failed: ${t.message}")
        }
    })
}
