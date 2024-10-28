package com.ilya.MeetingMap.SocialMap.Friends_Service

import FriendDB
import FriendDatabase
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface WebSocketListenerCallback {
    fun onMessageReceived(message: String)
    fun onErrorOccurred(error: String)
}

class WebSocketService(private val callback: WebSocketListenerCallback, private val context: Context) {

    private var webSocket: WebSocket? = null
    private var uid: String? = null
    private var key: String? = null
    @Inject
    lateinit var database: FriendDatabase // Экземпляр базы данных

    companion object {
        private const val TAG = "WebSocketService" // Тег для логов
    }

    init {
        try {
            database = FriendDatabase.getDatabase(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database: ${e.message}", e)
            callback.onErrorOccurred("Error initializing database: ${e.message}")
        }
    }


    fun connect(uid: String, key: String) {
        this.uid = uid
        this.key = key
        Log.d(TAG, "Attempting to connect with uid: $uid, key: $key")
        connectWebSocket()
    }

    private fun saveFriendsToDatabase(friendsJson: String) {
        try {
            // Преобразование JSON в список объектов FriendDB
            val friendsList = parseFriendsJson(friendsJson)

            // Проверка на пустоту списка
            if (friendsList.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        database.friendDao().insertFriends(friendsList)
                        Log.d(TAG, "Friends saved to database: ${friendsList.size}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving friends to database: ${e.message}", e)
                        callback.onErrorOccurred("Error saving friends: ${e.message}")
                    }
                }
            } else {
                Log.w(TAG, "No friends to save.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving friends to database: ${e.message}", e)
            callback.onErrorOccurred("Error saving friends: ${e.message}")
        }
    }

    private fun connectWebSocket() {
        if (uid == null || key == null) {
            Log.e(TAG, "UID or Key is null. Cannot connect WebSocket.")
            callback.onErrorOccurred("UID or Key is null.")
            return
        }

            //val url = "wss://meetmap.up.railway.app/get-friends/$uid/$key"
        val url = "wss://meetmap.up.railway.app/get-friends/NL85HoOb7FVYP8oDsPu1z9oml1o2/6GkAx0f6cJcWCmihMTpTe41IsqFMIV"
        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient.Builder()
            .pingInterval(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val webSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket connection opened. Sending initial request for friends...")
                webSocket.send("getFriends")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received from server: $text")
                callback.onMessageReceived(text) // Передача сообщения через интерфейс
                saveFriendsToDatabase(text) // Сохранение друзей в базе данных
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket connection failed: ${t.message}", t)
                callback.onErrorOccurred(t.message ?: "Unknown error") // Обработка ошибок через интерфейс
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket closed with code: $code, reason: $reason")
            }
        }

        try {
            webSocket = client.newWebSocket(request, webSocketListener)
            Log.d(TAG, "WebSocket connection initiated to URL: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate WebSocket connection: ${e.message}", e)
            callback.onErrorOccurred("Failed to connect: ${e.message}")
        }
    }

    fun closeWebSocket() {
        webSocket?.let {
            Log.d(TAG, "Closing WebSocket connection")
            it.close(1000, "Service destroyed")
        } ?: Log.w(TAG, "Attempted to close WebSocket but it was not initialized")
    }

    private fun parseFriendsJson(json: String): List<FriendDB> {
        val friendsList = mutableListOf<FriendDB>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val friendJson = jsonArray.getJSONObject(i)

                val token = friendJson.getString("token")
                val name = friendJson.getString("name")
                val img = friendJson.getString("img")
                val online = friendJson.getBoolean("online")

                // Проверка на наличие поля lastMessage
                val lastMessage = if (friendJson.has("lastMessage")) {
                    friendJson.getString("lastMessage")
                } else {
                    "No messages" // Установить значение по умолчанию, если поле отсутствует
                }

                val friend = FriendDB(token, name, img, online, lastMessage)
                friendsList.add(friend)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing friends JSON: ${e.message}", e)
            callback.onErrorOccurred("Error parsing JSON: ${e.message}")
        }
        return friendsList
    }

}