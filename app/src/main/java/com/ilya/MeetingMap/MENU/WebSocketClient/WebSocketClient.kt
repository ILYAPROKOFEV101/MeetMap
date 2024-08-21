package com.ilya.MeetingMap.MENU.WebSocketClient

import MarkerData
import android.util.Log

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString




class WebSocketClient {
    private val client = OkHttpClient()
    private lateinit var webSocket: okhttp3.WebSocket

    fun start(url: String) {
        val request = Request.Builder().url(url).build()
        val listener = EchoWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, "Goodbye!")
    }

    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to the server")
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            Log.d("WebSocket", "Received: $text")
            handleReceivedMessage(text)
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "Received bytes: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            Log.d("WebSocket", "Closing: $code / $reason")
        }

        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}", t)
        }

        private fun handleReceivedMessage(text: String) {
            // Преобразуем JSON в список объектов MarkerData
            try {
                val markers = Json.decodeFromString<List<MarkerData>>(text)
                // Далее можно обновить UI или выполнить другую обработку
                markers.forEach {
                    Log.d("WebSocket", "Marker: $it")
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Error parsing JSON: ${e.message}")
            }
        }
    }
}
