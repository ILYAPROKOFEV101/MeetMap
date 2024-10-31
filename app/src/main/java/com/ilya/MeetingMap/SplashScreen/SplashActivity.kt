package com.ilya.MeetingMap.SplashScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ilya.MeetingMap.MainActivity
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SocialMap {
                SplashScreen()
            }
        }

        // Задержка перед переходом на главный экран
        lifecycleScope.launch {
            delay(2000L) // Задержка 2 секунды
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    @Composable
    fun SplashScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF6200EE)), // цвет фона
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ваше Приложение",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

}
