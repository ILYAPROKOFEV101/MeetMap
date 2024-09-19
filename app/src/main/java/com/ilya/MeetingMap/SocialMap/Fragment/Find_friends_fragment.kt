package com.example.yourapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap

class Find_friends_fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                SocialMap {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        YourFragmentContent()
                    }
                }
            }
        }
    }
}

@Composable
fun YourFragmentContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hello, Jetpack Compose!", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { /* Handle button click */ }) {
            Text(text = "Click Me")
        }
    }

}
