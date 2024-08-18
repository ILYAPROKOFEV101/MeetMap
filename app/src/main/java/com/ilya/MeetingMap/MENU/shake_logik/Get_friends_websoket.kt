import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import okhttp3.*

import com.google.gson.Gson

class WebSocketManager(private val client: OkHttpClient) {

    private var webSocket: WebSocket? = null
    private val messages: MutableList<String> = mutableListOf()
    var isConnected: Boolean = false
        private set

    // Настройка WebSocket
    fun setupWebSocket(url: String) {
        if (!isConnected) {
            try {
                val request: Request = Request.Builder()
                    .url(url)
                    .build()

                Log.d("WebSocket", "Connecting to WebSocket: $url")
                webSocket = client.newWebSocket(request, object : WebSocketListener() {

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        handleReceivedMessage(text)
                    }

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        isConnected = true
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        isConnected = false
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        isConnected = false
                        Log.e("WebSocket", "WebSocket failure: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("WebSocket", "Error creating WebSocket: ${e.message}")
            }
        }
    }

    // Обработка полученного сообщения
    private fun handleReceivedMessage(message: String) {
        try {
            val gson = Gson()
            val receivedData = gson.fromJson(message, ReceivedData::class.java)
            // Делаем что-то с полученными данными
            Log.d("WebSocket", "Received data: ${receivedData.user_name}, ${receivedData.img}, ${receivedData.key}")
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing JSON: ${e.message}")
        }
    }

    // Функция закрытия WebSocket
    fun closeWebSocket() {
        webSocket?.close(1000, "Connection closed by user")
    }

    // Модель данных для полученного JSON
    data class ReceivedData(
        val user_name: String,
        val img: String,
        val key: String
    )
}
