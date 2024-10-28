import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.Identity
import com.ilya.chatmodule.DUO_Chat_Logick.Message
import com.ilya.chatmodule.DUO_Chat_Logick.MessageCallback
import com.ilya.chatmodule.DUO_Chat_Logick.MessageProcessor

class Find_friends_fragment : Fragment(), MessageCallback {
    private lateinit var messageProcessor: MessageProcessor
    private lateinit var roomId: String




    companion object {
        private const val ARG_ROOM_ID = "roomid"

        // Фабричный метод для создания нового экземпляра фрагмента с передачей roomid
        fun newInstance(roomId: String): Find_friends_fragment {
            val fragment = Find_friends_fragment()
            val args = Bundle()
            args.putString(ARG_ROOM_ID, roomId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
                // Инициализация MessageProcessor
        messageProcessor = MessageProcessor(this)
        messageProcessor.startProcessingMessages()

        return ComposeView(requireContext()).apply {

            setContent {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                        ) {

                        }
                    }

            }

        }

    }

    override fun onNewMessage(message: Message) {
        // Проверяем, что фрагмент все еще присоединен к активности
        activity?.runOnUiThread {
            // Например, обновляем список сообщений
            println("New message received: ${message.message} at ${message.time}")
            // Здесь можно обновить RecyclerView или другой UI компонент
        }
    }


    override fun onStart() {
        super.onStart()



            // Подключение к WebSocket
            val webSocketClient = Duo_chat_SocketClient() // Создание объекта без параметров
            val webSocketUrl = "wss://meetmap.up.railway.app/chat/$roomId?username=name&key=key&uid=uid&lastToken=0" // Замените на свой URL
            val listener = MyWebSocketListener(messageProcessor) // Инициализация слушателя
            webSocketClient.connect(webSocketUrl, listener) // Вызов метода connect с параметрами

    }








}