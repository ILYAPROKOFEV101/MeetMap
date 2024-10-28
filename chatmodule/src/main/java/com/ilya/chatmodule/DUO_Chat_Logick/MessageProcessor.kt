package com.ilya.chatmodule.DUO_Chat_Logick

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlinx.coroutines.*

interface MessageCallback {
    fun onNewMessage(message: Message)
}

class MessageProcessor(private val callback: MessageCallback) {
    private val messageQueue: BlockingQueue<Message> = ArrayBlockingQueue(100)

    // Функция добавления сообщения в очередь
    fun enqueueMessage(message: Message) {
        messageQueue.put(message)
    }

    // Запуск обработки сообщений в отдельной корутине
    fun startProcessingMessages() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val message = messageQueue.take()
                processMessage(message)
            }
        }
    }

    // Функция обработки сообщений (например, обновление UI)
    private fun processMessage(message: Message) {
        // Сообщаем обратно в Activity о новом сообщении
        callback.onNewMessage(message)
    }
}
