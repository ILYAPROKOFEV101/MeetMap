package com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ilya.MeetingMap.SocialMap.DataModel.Messages
import io.ktor.client.*
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
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> get() = _messages

    private val httpClient = HttpClient {
        install(WebSockets)
    }

    // Метод для переключения соединения на новый URL
    fun switchConnection(newUrl: String) {
        coroutineScope.launch {
            // Закрываем текущее соединение, если оно существует
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Switching to new URL"))
            webSocketSession = null

            try {
                // Подключаемся к новому URL
                webSocketSession = httpClient.webSocketSession(urlString = newUrl).also { session ->
                    receiveMessages(session)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Обработка ошибок подключения
            }
        }
    }


    // Получение и обработка сообщений от WebSocket
    private suspend fun receiveMessages(session: DefaultClientWebSocketSession) {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val messageJson = frame.readText()
                    // Десериализация JSON в объект Messages
                    val message = Json.decodeFromString<Messages>(messageJson)
                    // Обновляем состояние сообщений
                    _messages.value = listOf((_messages.value + message).toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Логирование ошибок
        }
    }

    // Метод для подключения к WebSocket
    fun connectToWebSocket(url: String) {
        coroutineScope.launch {
            try {
                webSocketSession = httpClient.webSocketSession(urlString = url).also { session ->
                    receiveMessages(session) // Получение сообщений при подключении
                }
            } catch (e: Exception) {
                e.printStackTrace() // Обработка ошибок подключения
            }
        }
    }


    fun sendMessage(message: String) {
        coroutineScope.launch {
            webSocketSession?.send(Frame.Text(message))
        }
    }

    fun disconnect() {
        coroutineScope.launch {
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Disconnecting"))
            webSocketSession = null
        }
    }

    private fun broadcastMessage(message: String) {
        val intent = Intent("com.yourapp.CHAT_MESSAGE")
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        disconnect() // Используйте disconnect для закрытия соединения
        coroutineScope.cancel() // Отменяем корутины
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

