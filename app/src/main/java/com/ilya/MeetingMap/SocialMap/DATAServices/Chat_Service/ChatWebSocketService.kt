package com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ilya.MeetingMap.SocialMap.DataModel.Messages
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json



class ChatWebSocketService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val _messages = MutableStateFlow<List<Messages>>(emptyList())
    val messages: StateFlow<List<Messages>> = _messages


    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }





    // Логирование для отладки
    private val TAG = "ChatWebSocketService"

    // Метод для переключения соединения на новый URL
    fun switchConnection(newUrl: String) {
        Log.d(TAG, "Switching connection to: $newUrl")
        coroutineScope.launch {
            // Закрываем текущее соединение, если оно существует
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Switching to new URL"))
            webSocketSession = null

            try {
                // Подключаемся к новому URL
                webSocketSession = httpClient.webSocketSession(urlString = newUrl).also { session ->
                    Log.d(TAG, "Successfully connected to $newUrl")
                    receiveMessages(session)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error switching connection: ${e.message}", e)
            }
        }
    }

    suspend fun receiveMessages(session: WebSocketSession) {
        try {
            for (message in session.incoming) {
                if (message is Frame.Text) {
                    val json = message.readText()
                    val receivedMessage = parseMessage(json)
                    _messages.emit(_messages.value + receivedMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving messages: ${e.message}", e)
        }
    }


    private val jsoname = Json {
        ignoreUnknownKeys = true // Игнорируем неизвестные ключи
    }     // Метод для парсинга JSON в объект Messages

    private fun parseMessage(json: String): Messages {
        return jsoname.decodeFromString<Messages>(json) // Используем настроенный Json
    }

    // Метод для подключения к WebSocket
    fun connectToWebSocket(url: String) {
        Log.d(TAG, "Connecting to WebSocket at: $url")
        coroutineScope.launch {
            try {
                webSocketSession = httpClient.webSocketSession(urlString = url).also { session ->
                    Log.d(TAG, "Successfully connected to WebSocket at $url")
                    receiveMessages(session) // Получение сообщений при подключении
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to WebSocket: ${e.message}", e) // Обработка ошибок подключения
            }
        }
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "Sending message: $message")
        coroutineScope.launch {
            webSocketSession?.send(Frame.Text(message))
            Log.d(TAG, "Message sent: $message")
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting from WebSocket")
        coroutineScope.launch {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Disconnecting"))
            webSocketSession = null
            Log.d(TAG, "Disconnected from WebSocket")
        }
    }

    fun broadcastMessage(message: String) {
        val intent = Intent("com.ilya.MeetingMap.NEW_MESSAGE")
        intent.putExtra("message_content", message)
        applicationContext.sendBroadcast(intent) // Применение applicationContext вместо context
        Log.d(TAG, "Broadcasting message: $message")
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        disconnect() // Используйте disconnect для закрытия соединения
        coroutineScope.cancel() // Отменяем корутины
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
