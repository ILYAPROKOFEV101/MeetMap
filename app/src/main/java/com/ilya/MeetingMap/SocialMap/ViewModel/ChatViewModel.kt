package com.ilya.MeetingMap.SocialMap.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service.ChatWebSocketService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> get() = _messages

    private val chatService = ChatWebSocketService()

    fun connectToChat(roomId: String, uid: String, key: String) {
        chatService.connectToWebSocket("wss://meetmap.up.railway.app/chat/cnyFlmsAIp4IJPy?username=John&uid=0vS1ksuo3IPUCd75u3MWMgUGhPp2&key=YG3HAXXVsW2Kuyb1U7zlm5PryOws4N&lastToken=0")

        // Подписка на изменения в сообщениях
        viewModelScope.launch {
            chatService.messages.collect { newMessages ->
                _messages.value = newMessages // Обновляем состояние в ViewModel
            }
        }
    }

    fun sendMessage(message: String) {
        chatService.sendMessage(message)
    }

    fun disconnectFromChat() {
        chatService.disconnect()
    }
}
