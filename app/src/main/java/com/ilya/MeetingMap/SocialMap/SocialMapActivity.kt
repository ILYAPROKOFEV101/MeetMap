package com.ilya.MeetingMap.SocialMap

import FriendDatabase
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter

import com.example.yourapp.ui.Find_friends_fragment
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ilya.MeetingMap.SocialMap.Friends_Service.Friend
import com.ilya.MeetingMap.SocialMap.Friends_Service.FriendsViewModel
import com.ilya.MeetingMap.SocialMap.Friends_Service.WebSocketListenerCallback


import com.ilya.MeetingMap.SocialMap.Friends_Service.WebSocketService


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

    private lateinit var database: FriendDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Инициализация базы данных
            database = FriendDatabase.getDatabase(this)
            // Другой код для инициализации вашего приложения
        } catch (e: Exception) {
            Log.e("SocialMapActivity", "Error initializing database: ${e.message}")
            e.printStackTrace()
        }

        // Инициализация WebSocketService
        webSocketService = WebSocketService(this, this)


        val uid = ID(userData = googleAuthUiClient.getSignedInUser())
        val key = getUserKey(this)

        webSocketService.connect(uid.toString(), key.toString())




        enableEdgeToEdge()
        setContent {
            SocialMap {
                val navController = rememberNavController()

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




    @Composable
    fun FriendsScreen(friendsList: List<Friend>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),

        ) {
            items(friendsList) { friend ->
                FriendItem(friend)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 70.dp)
                        .background(Color.Black)
                )
            }
        }
    }

    @Composable
    fun FriendItem(friend: Friend) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RectangleShape // Убираем закругление углов
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                AsyncImage(
                    model = friend.img,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier

                        .align(Alignment.CenterVertically)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .width(60.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    verticalArrangement = Arrangement.Top, // Выравнивание по верхнему краю
                    modifier = Modifier.fillMaxHeight() // Заставляет Column занять всю доступную высоту
                ) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = friend.lastmessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )



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
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) // Используем цвет из темы
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
                    }
                }
            }
        }
    }










}