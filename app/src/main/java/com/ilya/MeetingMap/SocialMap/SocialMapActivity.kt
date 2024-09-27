package com.ilya.MeetingMap.SocialMap

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Card
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yourapp.ui.Find_friends_fragment
import com.ilya.MeetingMap.R
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap

class SocialMapActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialMap {

                    val navController = rememberNavController()

                    Column(Modifier.fillMaxSize()) {
                        FindFriends()
                           /* NavHost(
                                navController = navController,
                                startDestination = "Friendsearch"

                            ) {
                                composable("Friendsearch") {
                                    Findfriendsfragment()
                                }

                            }*/
                        }
                    }
        }




    }


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun FindFriends() {
        var expanded by remember { mutableStateOf(false) }
        var tapCount by remember { mutableStateOf(0) }  // Переменная для подсчета нажатий

        // Анимируем высоту карточки
        val cardHeight by animateDpAsState(
            targetValue = if (expanded) 800.dp else 200.dp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .border(4.dp, Color.Blue, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = !expanded } // Переключаем состояние при нажатии на карту
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .height(750.dp)
                        .pointerInteropFilter { event ->
                            // Обрабатываем только два касания, после чего позволяем событиям идти в Fragment
                            if (event.action == MotionEvent.ACTION_DOWN) {

                                tapCount += 1
                                if (tapCount == 2) {
                                    expanded = !expanded
                                    tapCount = 0 // Сброс после двух нажатий
                                    true // Перехватываем второе касание
                                } else {
                                    false // Позволяем передать событие фрагменту
                                }
                            } else {
                                false // Для других событий ничего не делаем
                            }
                        }
                ) {
                    // Фрагмент или его содержимое
                    AndroidView(
                        factory = { context ->
                            FragmentContainerView(context).apply {
                                id = View.generateViewId()
                            }
                        },
                        update = { view ->
                            val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            val findFriendsFragment = Find_friends_fragment()
                            fragmentTransaction.replace(view.id, findFriendsFragment)
                            fragmentTransaction.commit()
                        }
                    )
                }
                IconButton(
                    onClick = { expanded = false }, // Закрываем карточку по нажатию
                    modifier = Modifier // Иконка закрытия в верхнем правом углу
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Иконка закрытия
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }






    @Composable
    fun FindFriendsFragment() {
        AndroidView(
            factory = { context ->
                // Создаем FragmentContainerView
                FragmentContainerView(context).apply {
                    id = View.generateViewId()
                }
            },
            update = { view ->
                // Получаем FragmentManager
                val fragmentManager = (view.context as FragmentActivity).supportFragmentManager
                // Создаем и добавляем Chatmenu фрагмент
                val fragmentTransaction = fragmentManager.beginTransaction()
                val Find_friends_fragment = Find_friends_fragment()
                fragmentTransaction.replace(view.id, Find_friends_fragment)
                fragmentTransaction.commit()
            },
            modifier = Modifier
                .pointerInput(Unit) {  // Перехватываем касания
                    detectTapGestures(onTap = {}) // Пустая обработка касаний
                }
        )

    }


}