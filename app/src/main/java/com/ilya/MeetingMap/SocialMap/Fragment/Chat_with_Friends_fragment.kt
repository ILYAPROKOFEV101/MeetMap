package com.example.yourapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter

import com.google.android.gms.auth.api.identity.Identity
import com.ilya.MeetingMap.SocialMap.ui.theme.SocialMap
import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.reaction.logik.PreferenceHelper.getUserKey


class Chat_with_Friends_fragment : Fragment() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }


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

                    }
                }
            }
            
        }

    }


    @Composable
    @Preview
    fun Chat_with_Friends()
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )
        {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.2f)
            )
            {
                Image(
                    painter = rememberAsyncImagePainter(""),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(90.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(0.7f)
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f)
                )
                {
                    Text(text = "")
                }

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                )
                {
                    Text(text = "")
                }

            }

            Column(modifier = Modifier.fillMaxHeight().weight(0.1f)) {

            }
        }
    }


    override fun onStart() {
        super.onStart()
        val uid = ID(
            userData = googleAuthUiClient.getSignedInUser()
        )
        val key = getUserKey(requireContext())


    }

    override fun onStop() {
        super.onStop()

    }




}
