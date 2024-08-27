package com.ilya.MeetingMap.MENU.WebSocketClient

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import com.ilya.MeetingMap.R

fun show_friends_one(contex: Context, data: List<Friends_type>) {
    val dialogView = LayoutInflater.from(contex).inflate(R.layout.friends_list, null)
    val Icon = dialogView.findViewById<ImageView>(R.id.Icon_user)
    val name  = dialogView.findViewById<TextView>(R.id.text_name)
    val botton_add_frinds = dialogView.findViewById<Button>(R.id.person_add)


    name.text = data[0].name

}
