package com.ilya.MeetingMap.SocialMap.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ilya.MeetingMap.SocialMap.DataModel.FindFriends


class FriendsViewModel_data : ViewModel() {
    // Список друзей, который будет обновляться
    private val _friendsList = mutableStateOf<List<FindFriends>>(emptyList())
    val friendsList: State<List<FindFriends>> = _friendsList

    // Метод для обновления списка друзей
    fun updateFriendsList(friends: List<FindFriends>) {
        _friendsList.value = friends
    }
}
