import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
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

class WebSocketManager(
    private val client: OkHttpClient,
    private val callback: WebSocketCallback?
) {

    private var webSocket: WebSocket? = null
    private val messages: MutableList<String> = mutableListOf()
    var isConnected: Boolean = false
        private set

    private val handlerThread: HandlerThread = HandlerThread("WebSocketThread")
    private lateinit var handler: Handler
    private var canConnect = true

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    // Настройка WebSocket
    fun setupWebSocket(url: String) {
        Log.d("WebSocket", "url = $url")
        if (!canConnect) {
            Log.d("WebSocket", "Подключение слишком часто. Подождите 10 секунд.")
            return
        }

        if (isConnected) {
            closeWebSocket()
        }

        try {
            val request: Request = Request.Builder()
                .url(url)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleReceivedMessage(text)
                }

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                    canConnect = false
                    Log.d("WebSocket", "WebSocket opened with response: $response")
                    handler.postDelayed({
                        canConnect = true
                        Log.d("WebSocket", "Можно повторно подключаться к WebSocket")
                    }, 10 * 1000)  // Установлена задержка на 10 секунд
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                    Log.d("WebSocket", "WebSocket closed with reason: $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                    Log.e("WebSocket", "WebSocket failure: ${t.message}. Response: $response")
                }
            })
        } catch (e: Exception) {
            Log.e("WebSocket", "Error creating WebSocket: ${e.message}")
        }
    }


    // Обработка полученного сообщения
    fun handleReceivedMessage(message: String) {
        try {
            val gson = Gson()
            val receivedData = gson.fromJson(message, ReceivedData::class.java)
            Log.d("WebSocket", "Received data: ${receivedData.user_name}, ${receivedData.img}, ${receivedData.key}")
            // Возвращаем данные через callback
            callback?.onMessageReceived(receivedData)
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
