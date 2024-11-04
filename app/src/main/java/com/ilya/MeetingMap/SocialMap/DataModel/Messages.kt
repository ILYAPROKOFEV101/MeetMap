package com.ilya.MeetingMap.SocialMap.DataModel

import kotlinx.serialization.Serializable


@Serializable
data class Messages(
    val content: String,
    val imageUrl: String,
    val key: String,
    val messageTime: Long,
    val senderUsername: String,
    val timestamp: Long
)