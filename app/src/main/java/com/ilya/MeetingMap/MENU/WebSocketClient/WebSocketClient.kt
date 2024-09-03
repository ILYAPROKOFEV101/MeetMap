package com.ilya.MeetingMap.MENU.WebSocketClient

import MarkerData
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString


class  WebSocketClient(private val url: String) : WebSocketListener() {
    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket
    private var deferredResponse: CompletableDeferred<String>? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket_shake", "Connected to the server")
            onConnectionOpened()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket_shake", "Received message: $text")
            handleResponse(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket_shake", "Closing: $code / $reason")
            onConnectionClosing(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket_shake", "Error: ${t.message}", t)
            deferredResponse?.completeExceptionally(t)
            onConnectionFailure(t, response)
            retryConnection()
        }
    }

    fun start() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        if (::webSocket.isInitialized) {
            webSocket.send(message)
            Log.d("WebSocket_shake", "Sent: $message")
        } else {
            Log.e("WebSocket_shake", "WebSocket is not initialized!")
        }
    }

    fun close() {
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Goodbye!")
        } else {
            Log.e("WebSocket_shake", "WebSocket is not initialized!")
        }
    }

    open fun onConnectionOpened() {}
    open fun onConnectionClosing(code: Int, reason: String) {}
    open fun onConnectionFailure(t: Throwable, response: Response?) {}

    fun sendCommandAndGetResponse(command: String): Deferred<String> {
        deferredResponse = CompletableDeferred()
        sendMessage(command)
        return deferredResponse!!
    }

    private fun handleResponse(text: String) {
        Log.d("WebSocket_shake", "Received message: $text")
        deferredResponse?.let {
            it.complete(text)
            deferredResponse = null
        }
    }

    private fun retryConnection() {
        Log.d("WebSocket_shake", "Retrying connection in 2 seconds...")
        Handler(Looper.getMainLooper()).postDelayed({
            start()
        }, 2000)
    }
}