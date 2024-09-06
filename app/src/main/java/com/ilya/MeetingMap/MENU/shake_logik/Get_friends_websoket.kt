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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import okhttp3.WebSocketListener

import okhttp3.*

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.logging.HttpLoggingInterceptor

class WebSocketManager(private val client: OkHttpClient, private val callback: WebSocketCallback?) : WebSocketListener() {

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
        Log.d("WebSocket_shake", "url = $url")

        if (!canConnect) {
            Log.d("WebSocket_shake", "Подключение слишком часто. Подождите 10 секунд.")
            return
        }

        try {
            // Закрываем предыдущий WebSocket, если он существует
            webSocket?.close(1000, "Reconnecting")

            val request: Request = Request.Builder()
                .url(url)
                .build()

            webSocket = client.newWebSocket(request, this)
        } catch (e: Exception) {
            Log.e("WebSocket_shake", "Error creating WebSocket: ${e.message}")
        }
    }

    // Обработка события открытия сокета
    override fun onOpen(webSocket: WebSocket, response: Response) {
        isConnected = true
        canConnect = false
        Log.d("WebSocket_shake", "WebSocket opened with response: $response")
    }

    // Обработка события получения сообщения
    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket_shake", "Raw message received: $text")
        handleReceivedMessage(text)
        webSocket.close(1000, "Goodbye!")
    }

    // Обработка события закрытия сокета
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        isConnected = false
        Log.d("WebSocket_shake", "WebSocket closed with reason: $reason")

    }

    // Обработка ошибки подключения
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnected = false
        Log.e("WebSocket_shake", "WebSocket failure: ${t.message}. Response: $response")
    }

    // Обработка полученного сообщения
    private fun handleReceivedMessage(message: String) {
        try {
            val gson = Gson()
            val receivedDataList: List<ReceivedData> = try {
                gson.fromJson(message, Array<ReceivedData>::class.java).toList()
            } catch (e: JsonSyntaxException) {
                listOf(gson.fromJson(message, ReceivedData::class.java))
            }

            Log.d("WebSocket_shake", "Received data count: ${receivedDataList.size}")
            Log.d("WebSocket_shake", "Received data: $receivedDataList")

            // Возвращаем данные через callback
            callback?.onMessageReceived(receivedDataList)
        } catch (e: Exception) {
            Log.e("WebSocket_shake", "Error parsing JSON: ${e.message}")
        }
    }

    // Функция закрытия WebSocket
    fun closeWebSocket() {
        webSocket?.close(1000, "Connection closed by user")
        webSocket = null
    }

    // Модель данных для полученного JSON
    data class ReceivedData(
        val user_name: String,
        val img: String,
        val key: String
    )



    // Остановка потока и освобождение ресурсов
    fun shutdown() {
        closeWebSocket()
        handlerThread.quitSafely()
    }
}
