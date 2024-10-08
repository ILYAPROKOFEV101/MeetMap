package com.ilya.chatmodule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap


import com.ilya.chatmodule.ui.theme.MeetMapTheme

class ChatActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialMap {
                val navController = rememberNavController()

                Column(
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {

                    NavHost(
                        navController = navController,
                        startDestination = "Friendsearch"

                    ) {
                        composable("Friendsearch") {

                        }

                    }
                }

            }
        }


    }

}