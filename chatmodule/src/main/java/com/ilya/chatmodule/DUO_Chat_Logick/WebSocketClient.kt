import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class Duo_chat_SocketClient {
    private val client: OkHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false

    fun connect(url: String, listener: WebSocketListener) {
        if (!isConnected) {
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, listener)
            isConnected = true
        }
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Client closed the connection")
        isConnected = false
    }
}
