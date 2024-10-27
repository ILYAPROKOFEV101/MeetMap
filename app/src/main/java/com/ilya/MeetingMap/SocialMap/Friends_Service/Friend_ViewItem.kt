package com.ilya.MeetingMap.SocialMap.Friends_Service

import FriendDB
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.json.JSONArray


data class Friend(
    val img: String,
    val lastmessage: String,
    val name: String,
    val online: Boolean,
    val token: String
)

private fun parseFriendsJson(json: String): List<FriendDB> {
    val friendsList = mutableListOf<FriendDB>()

    // Предполагаем, что JSON массив объектов с друзьями
    val jsonArray = JSONArray(json)
    for (i in 0 until jsonArray.length()) {
        val friendJson = jsonArray.getJSONObject(i)

        val token = friendJson.getString("token")
        val name = friendJson.getString("name")
        val img = friendJson.getString("img")
        val online = friendJson.getBoolean("online")
        val lastMessage = friendJson.getString("lastMessage")

        val friend = FriendDB(token, name, img, online, lastMessage)
        friendsList.add(friend)
    }
    return friendsList
}


class FriendsViewModel : ViewModel() {
    val friendsList = mutableStateListOf<Friend>() // Используем для хранения друзей

    // Функция для обновления данных друзей
    fun updateFriends(newFriends: List<Friend>) {
        viewModelScope.launch(Dispatchers.Main) {
            friendsList.clear()
            friendsList.addAll(newFriends)
        }
    }
}