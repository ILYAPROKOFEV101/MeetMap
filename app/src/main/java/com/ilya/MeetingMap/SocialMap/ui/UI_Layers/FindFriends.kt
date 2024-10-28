package com.ilya.MeetingMap.SocialMap.ui.UI_Layers

import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.example.yourapp.ui.Find_friends_fragment

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