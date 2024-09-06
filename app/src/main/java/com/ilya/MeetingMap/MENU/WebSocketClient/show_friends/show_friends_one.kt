package com.ilya.MeetingMap.MENU.WebSocketClient.show_friends

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import com.ilya.MeetingMap.R
import com.bumptech.glide.Glide
import com.ilya.MeetingMap.MENU.WebSocketClient.Friends_type
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter

import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

 fun show_friends_one(context: Context, data: List<Friends_type>) {
    if (data.isEmpty()) return  // Проверка на пустой список

    // Inflate the custom layout for the dialog
    val dialogView = LayoutInflater.from(context).inflate(R.layout.friends_list, null)
    val konfettiView = dialogView.findViewById<KonfettiView>(R.id.konfettiView)

    // Find views inside the custom layout
    val icon = dialogView.findViewById<ImageView>(R.id.Icon_user)
    val name = dialogView.findViewById<TextView>(R.id.textname)
    val buttonAddFriends = dialogView.findViewById<Button>(R.id.person_add)

    // Populate the views with data
    name.text = data[0].name

    // Load the image into the ImageView using Glide
    Glide.with(context)
        .load(data[0].img)
        .into(icon)

    // Set up the button click listener
    buttonAddFriends.setOnClickListener {
        // Handle the add friend action
    }

    // Dismiss the current dialog if it's already shown
   // currentDialog?.dismiss()

    // Build and show the new dialog
    val alertDialog = AlertDialog.Builder(context, R.style.CustomDialog)
        .setView(dialogView)
        .create()

    // Remove default background
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    // Set dialog to center of the screen
    alertDialog.window?.setLayout(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    alertDialog.show()

    // Center the dialog
    val layoutParams = WindowManager.LayoutParams()
    layoutParams.copyFrom(alertDialog.window?.attributes)
    layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
    layoutParams.gravity = Gravity.CENTER
    alertDialog.window?.attributes = layoutParams

    // Save the current dialog reference
    //currentDialog = alertDialog

    // Set up the konfetti
    val emitterConfig = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)

    konfettiView.start(
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(Color.YELLOW, Color.GREEN, Color.MAGENTA),
            position = Position.Relative(0.5, 0.0),
            size = listOf(Size.SMALL, Size.LARGE),
            timeToLive = 3000L,
            shapes = listOf(Shape.Square),
            emitter = emitterConfig
        )
    )
}