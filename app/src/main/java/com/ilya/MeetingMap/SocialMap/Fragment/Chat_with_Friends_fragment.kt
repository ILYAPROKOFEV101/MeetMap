package com.example.yourapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter

import com.google.android.gms.auth.api.identity.Identity
import com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service.ChatWebSocketService
import com.ilya.MeetingMap.SocialMap.Interface.DataListener
import com.ilya.MeetingMap.SocialMap.ViewModel.ChatViewModel
import com.ilya.MeetingMap.SocialMap.ViewModel.FriendsViewModel
import com.ilya.MeetingMap.SocialMap.ViewModel.FriendsViewModel_data
import com.ilya.MeetingMap.SocialMap.ui.UI_Layers.ChatScreen
import com.ilya.MeetingMap.SocialMap.ui.UI_Layers.MessageList
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap
import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.profile.IMG
import com.ilya.codewithfriends.presentation.profile.UID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.reaction.logik.PreferenceHelper.getUserKey


class Chat_with_Friends_fragment : Fragment() {

    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    private val friendsViewModel: FriendsViewModel by viewModels()



    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                 val key = ""
                val name = UID(userData = googleAuthUiClient.getSignedInUser())
                val img = IMG(userData = googleAuthUiClient.getSignedInUser())
                val uid = ID(userData = googleAuthUiClient.getSignedInUser())


                SocialMap {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        /*ChatScreen(
                                chatViewModel = chatViewModel
                                )*/
                        MessageList(chatViewModel = chatViewModel, username = name.toString(), my_avatar = img.toString(), my_key = key.toString())
                    }
                }
            }
        }
    }



    override fun onStart() {
        super.onStart()
        // Запуск сервиса для WebSocket
        requireContext().startService(Intent(requireContext(), ChatWebSocketService::class.java))
        val dataListener = object : DataListener {
            override fun onDataReceived(data: String) {
                // Обрабатываем полученные данные (например, сохраняем в базе данных или UI)
                Log.d("DataListener", "Получены данные: $data")
            }
        }
        val token = friendsViewModel.getFriendToken()
        Log.d("Save_token", "получаю " + dataListener)
        val uid = ID(userData = googleAuthUiClient.getSignedInUser())
        val key = getUserKey(requireContext())
        chatViewModel.connectToChat(token.toString(), uid.toString(), key.toString())
    }

    override fun onStop() {
        super.onStop()
        requireContext().stopService(Intent(requireContext(), ChatWebSocketService::class.java))
        chatViewModel.disconnectFromChat()
    }
}

