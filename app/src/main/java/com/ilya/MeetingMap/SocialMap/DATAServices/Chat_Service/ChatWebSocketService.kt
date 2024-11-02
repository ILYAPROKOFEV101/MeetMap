package com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class ChatWebSocketService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val chatConnections = mutableMapOf<String, DefaultClientWebSocketSession>()

    private val httpClient = HttpClient {
        install(WebSockets)
    }

    // Метод для подключения к новому чату
    fun connectToChat(friendId: String, chatUrl: String) {
        if (chatConnections.containsKey(friendId)) return // Уже подключены

        coroutineScope.launch {
            try {
                httpClient.webSocketSession(urlString = chatUrl).let { session ->
                    chatConnections[friendId] = session
                    receiveMessages(friendId, session)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Логгирование ошибки подключения
            }
        }
    }

    // Получение сообщений из конкретного WebSocket-соединения
    private suspend fun receiveMessages(friendId: String, session: DefaultClientWebSocketSession) {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    broadcastMessage(friendId, message) // Отправка сообщения через BroadcastReceiver
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Логгирование ошибок во время приема сообщений
        }
    }

    // Метод для отключения от чата
    suspend fun disconnectFromChat(friendId: String) {
        chatConnections[friendId]?.close(CloseReason(CloseReason.Codes.NORMAL, "User left the chat"))
        chatConnections.remove(friendId)
    }

    private fun broadcastMessage(friendId: String, message: String) {
        val intent = Intent("com.yourapp.CHAT_MESSAGE")
        intent.putExtra("friendId", friendId)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        coroutineScope.launch {
            chatConnections.values.forEach { session ->
                session.close(CloseReason(CloseReason.Codes.NORMAL, "Service stopped"))
            }
            chatConnections.clear()
        }
        coroutineScope.cancel()
        super.onDestroy()
    }

}
