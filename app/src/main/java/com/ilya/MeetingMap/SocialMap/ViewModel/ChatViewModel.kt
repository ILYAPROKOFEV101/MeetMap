package com.ilya.MeetingMap.SocialMap.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service.ChatWebSocketService
import com.ilya.MeetingMap.SocialMap.DataModel.Messages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Messages>>(emptyList())
    val messages: StateFlow<List<Messages>> get() = _messages

    private val chatService = ChatWebSocketService()

    fun connectToChat(roomId: String, uid: String, key: String) {
        chatService.connectToWebSocket("wss://meetmap.up.railway.app/chat/$roomId?username=YourUsername&uid=$uid&key=$key")

        viewModelScope.launch {
            chatService.messages.collect { newMessages ->
                _messages.value = newMessages
            }
        }
    }

    fun sendMessage(content: String) {

        chatService.sendMessage(content) // Используйте kotlinx.serialization для преобразования в JSON
    }

    fun disconnectFromChat() {
        chatService.disconnect()
    }
}

