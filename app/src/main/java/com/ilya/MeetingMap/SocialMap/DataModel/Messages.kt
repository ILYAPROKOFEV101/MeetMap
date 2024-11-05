package com.ilya.MeetingMap.SocialMap.DataModel

import kotlinx.serialization.Serializable


@Serializable
data class Messages(
    val content: String? = null,
    val imageUrl: String? = null,
    val messageTime: Long? = null,
    val senderUsername: String? = null,
    val timestamp: Long? = null
)
