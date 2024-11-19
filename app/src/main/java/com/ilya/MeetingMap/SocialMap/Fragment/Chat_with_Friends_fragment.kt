package com.example.yourapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.auth.api.identity.Identity
import com.ilya.MeetingMap.SocialMap.DATAServices.Chat_Service.ChatWebSocketService

import com.ilya.MeetingMap.SocialMap.Interface.MyDataProvider
import com.ilya.MeetingMap.SocialMap.ViewModel.ChatViewModel
import com.ilya.MeetingMap.SocialMap.ViewModel.FriendsViewModel
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



                val name = UID(userData = googleAuthUiClient.getSignedInUser())
                val img = IMG(userData = googleAuthUiClient.getSignedInUser())
                val key = getUserKey(requireContext())



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



            requireContext().startService(
                Intent(
                    requireContext(),
                    ChatWebSocketService::class.java
                )
            )



        var token  =   MyDataProvider(this.requireContext()).getToken() // Retrieve the token
        Log.d("Save_token", "получаю токен: ${token.toString()}")
        val uid = ID(userData = googleAuthUiClient.getSignedInUser())
        val name = UID(userData = googleAuthUiClient.getSignedInUser())
        val key = getUserKey(requireContext())

        chatViewModel.connectToChat(token.toString(), uid.toString(), key.toString(), name.toString())
    }

    override fun onStop() {
        super.onStop()
        requireContext().stopService(Intent(requireContext(), ChatWebSocketService::class.java))
        chatViewModel.disconnectFromChat()
    }

}

