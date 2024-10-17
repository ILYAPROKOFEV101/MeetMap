import com.google.gson.Gson
import com.ilya.chatmodule.DUO_Chat_Logick.Message
import com.ilya.chatmodule.DUO_Chat_Logick.MessageProcessor
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MyWebSocketListener(private val messageProcessor: MessageProcessor) : WebSocketListener() {
    private val gson = Gson()

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        println("Connected to WebSocket")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Парсим сообщение и добавляем его в очередь для последующей обработки
        val message = gson.fromJson(text, Message::class.java)
        println("Received message: $message")

        // Добавляем сообщение в очередь для фоновоой обработки
        messageProcessor.enqueueMessage(message)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Обработка бинарных сообщений, если необходимо
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("WebSocket closed: $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        println("Error: ${t.message}")
    }
}

