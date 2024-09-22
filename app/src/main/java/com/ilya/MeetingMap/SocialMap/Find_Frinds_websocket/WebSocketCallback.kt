package com.ilya.MeetingMap.SocialMap.Find_Frinds_websocket

import Friend

interface WebSocketCallback_frinds {
    fun onFriendListReceived(friends: List<Friend>)
}