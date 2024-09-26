package com.example.yourapp.ui

import Friend
import FriendsViewModel
import WebSocketFindFriends
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout.Alignment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment

import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.search.SearchBar
import com.ilya.MeetingMap.R
import com.ilya.MeetingMap.SocialMap.Find_Frinds_websocket.WebSocketCallback_frinds
import com.ilya.MeetingMap.SocialMap.ui.theme.CustomTypography
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap
import com.ilya.MeetingMap.robotoBold
import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.reaction.logik.PreferenceHelper.getUserKey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.ilya.MeetingMap.SocialMap.ui.theme.robotomedium
import kotlinx.coroutines.*
import postRequestAddFriends


class Find_friends_fragment : Fragment(), WebSocketCallback_frinds {
    private var username by mutableStateOf("")
    private var friendsList by mutableStateOf(emptyList<Friend>())

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }

    private lateinit var webSocketFindFriends: WebSocketFindFriends
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return ComposeView(requireContext()).apply {

            setContent {
                SocialMap {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            // Убираем weight у Box
                            Box(
                                Modifier
                                    .height(150.dp) // Занимает только необходимое место
                                    .fillMaxWidth()
                            ) {
                                SearchBar() // Поисковая строка
                            }

                            // FriendsList займёт оставшееся пространство
                            FriendsList(friendsList)
                        }
                    }
                }
            }
            
        }

    }


    override fun onStart() {
        super.onStart()
        val uid = ID(
            userData = googleAuthUiClient.getSignedInUser()
        )
        val key = getUserKey(requireContext())
        if (key != null && uid != null) {
            webSocketFindFriends =
                WebSocketFindFriends("wss://meetmap.up.railway.app/findFriends/$uid/$key", this)
            Log.d(
                "Websocket_friends",
                "wss://meetmap.up.railway.app/findFriends/$uid/$key"
            )
        }

    }

    override fun onStop() {
        super.onStop()
        // Отключаемся от WebSocket, как только фрагмент становится невидимым
        webSocketFindFriends.stop()
    }

    override fun onFriendListReceived(friends: List<Friend>) {
        friendsList = friends // Обновляем состояние )
        Log.d("Websocket_friends", friends.toString())
    }

    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class,
        DelicateCoroutinesApi::class
    )
    @Preview
    @Composable
    fun SearchBar() {
        val keyboardControllers = LocalSoftwareKeyboardController.current

        SocialMap { // Оборачиваем в нашу кастомную тему
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Цвет зависит от темы
                    .padding(start = 30.dp, end = 30.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                // Текст с кастомным шрифтом и цветом
                Text(
                    text = stringResource(id = R.string.write_user_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground, // Цвет текста в зависимости от темы
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                // Поле ввода
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            border = BorderStroke(
                                3.dp,
                                SolidColor(MaterialTheme.colorScheme.onSurface)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    value = username,
                    onValueChange = { username = it },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        containerColor = MaterialTheme.colorScheme.surface, // Цвет поверхности
                        cursorColor = MaterialTheme.colorScheme.onSurface, // Цвет курсора
                        focusedTextColor = MaterialTheme.colorScheme.onSurface // Цвет текста
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardControllers?.hide()
                            webSocketFindFriends.sendCommand("findFriends $username")
                        }
                    ),
                )
            }


        }


             


        LaunchedEffect(key1 = username) {
            while (isActive) {
                delay(3000)  // Ждем 5 секунд
                // Отправляем команду каждые 5 секунд
                if(username != ""){
                    webSocketFindFriends.sendCommand("findFriends $username")
                }

            }
        }
        //    webSocketFindFriends.sendCommand("findFriends $username")

    }


    @Composable
    fun FriendsList(friends: List<Friend>) {
        val key = getUserKey(requireContext())
        SocialMap {
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.background) // Цвет зависит от темы
                    .padding(start = 30.dp, end = 30.dp),

            )
            LazyColumn(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                items(friends) { friend ->
                    if(friend.key == key) return@items
                    FriendItem(friend)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    @Composable
    fun FriendItem(friend: Friend) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
                .height(100.dp)
            //.padding(8.dp)
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface) // Цвет фона поверхности
            ) {
                Image(
                    painter = rememberAsyncImagePainter(friend.img),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(90.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                // Имя друга
                Text(
                    text = friend.name,
                    fontFamily = robotomedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface // Цвет текста
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Кнопка
                Button(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentHeight(),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            postRequestAddFriends(uid = getUserKey(requireContext()).toString(), key = getUserKey(requireContext()).toString(), friendKey = friend.key)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary, // Цвет кнопки
                        contentColor = MaterialTheme.colorScheme.onPrimary // Цвет текста на кнопке
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.addfriends),
                        fontFamily = robotomedium,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
