import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FriendsViewModel : ViewModel() {

    // Храним список друзей как состояние
    var friendsList by mutableStateOf(emptyList<Friend>())
        private set

    // Функция для обновления списка друзей
    fun updateFriendsList(newFriends: List<Friend>) {
        friendsList = newFriends
    }
}
