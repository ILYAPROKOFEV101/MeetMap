package com.ilya.MeetingMap.SocialMap

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yourapp.ui.Find_friends_fragment
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap

class SocialMapActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialMap {

                    val navController = rememberNavController()

                    Column(Modifier.fillMaxSize()) {

                            NavHost(
                                navController = navController,
                                startDestination = "Friendsearch"

                            ) {
                                composable("Friendsearch") {
                                    Findfriendsfragment()
                                }
                            }
                        }
                    }
        }




    }
    @Composable
    fun Findfriendsfragment() {
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
            }
        )

    }


}