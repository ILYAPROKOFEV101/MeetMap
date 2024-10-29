package com.ilya.MeetingMap.SocialMap


import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
<<<<<<< HEAD
import androidx.compose.material3.MaterialTheme

=======
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
>>>>>>> 724272f ( new ui)
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ilya.MeetingMap.SocialMap.DataModel.Friend

import com.ilya.MeetingMap.SocialMap.ViewModel.FriendsViewModel
import com.ilya.MeetingMap.SocialMap.DATAServices.WebSocketListenerCallback


import com.ilya.MeetingMap.SocialMap.DATAServices.WebSocketService
import com.ilya.MeetingMap.SocialMap.ui.UI_Layers.FindFriends
import com.ilya.MeetingMap.SocialMap.ui.UI_Layers.FriendsScreen


import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap

import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.reaction.logik.PreferenceHelper.getUserKey


class SocialMapActivity : FragmentActivity(), WebSocketListenerCallback {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var webSocketService: WebSocketService
    private val friendsViewModel: FriendsViewModel by viewModels() // Используем ViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Инициализация WebSocketService
        webSocketService = WebSocketService(this, this)


        val uid = ID(userData = googleAuthUiClient.getSignedInUser())
        val key = getUserKey(this)

        webSocketService.connect(uid.toString(), key.toString())




        enableEdgeToEdge()
        setContent {
            SocialMap {
                val navController = rememberNavController()

<<<<<<< HEAD
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    NavHost(navController = navController, startDestination = "Chatmenu") {
                        composable("Friendsearch") { FindFriends() }
                        composable("Chatmenu") {
                            FriendsScreen(friendsViewModel.friendsList)
                        }
=======
                    val navController = rememberNavController()

                    Column(Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                    ) {

                           NavHost(
                                navController = navController,
                                startDestination = "Friendsearch"

                            ) {
                                composable("Friendsearch") {
                                    FindFriends()
                                }

                            }
                        }

            }
        }




    }


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun FindFriends() {
        var expanded by remember { mutableStateOf(false) }
        var tapCount by remember { mutableStateOf(0) }

        // Анимируем высоту карточки
        val cardHeight by animateDpAsState(
            targetValue = if (expanded) 800.dp else 200.dp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .height(cardHeight)
                .border(4.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) // Используем цвет из темы
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = !expanded }, // Переключаем состояние при нажатии
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer // Цвет контейнера карты
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .height(cardHeight - 50.dp)
                ) {
                    // Фрагмент или его содержимое
                    AndroidView(
                        factory = { context ->
                            FragmentContainerView(context).apply {
                                id = View.generateViewId()
                            }
                        },
                        update = { view ->
                            val fragmentManager =
                                (view.context as FragmentActivity).supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            val findFriendsFragment = Find_friends_fragment()
                            fragmentTransaction.replace(view.id, findFriendsFragment)
                            fragmentTransaction.commit()
                        }
                    )
                }
                if (!expanded) {
                    IconButton(
                        onClick = { expanded =  !expanded},
                        modifier = Modifier.align(Alignment.End) // Иконка в правом верхнем углу
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Открыть",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer, // Цвет иконки
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { expanded = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer, // Цвет иконки
                            modifier = Modifier.size(32.dp)
                        )
>>>>>>> 724272f ( new ui)
                    }
                }
            }
        }


    }



    override fun onStop() {
        super.onStop()
        val intent = Intent(this, WebSocketService::class.java)
        stopService(intent) // Останавливаем WebSocketService
    }


        // Реализация методов интерфейса для обработки сообщений и ошибок
        override fun onMessageReceived(message: String) {
            val listType = object : TypeToken<List<Friend>>() {}.type
            val newFriendsList: List<Friend> = Gson().fromJson(message, listType)


            // Обновляем данные в ViewModel
            friendsViewModel.updateFriends(newFriendsList)
        }




        override fun onErrorOccurred(error: String) {
        // Обработка ошибок
        println("Error occurred: $error")
        // Здесь вы можете уведомить пользователя об ошибке
     }


    override fun onDestroy() {
        super.onDestroy()
        webSocketService.closeWebSocket()
    }






<<<<<<< HEAD















=======
>>>>>>> 724272f ( new ui)
}