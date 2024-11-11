package com.ilya.MeetingMap.SocialMap.DataModel

import kotlinx.serialization.Serializable


@Serializable
data class Messages(
    val content: String? = null,
    val profilerIMG: String? = null,
    val messageTime: Long? = null,
    val key: String? = null,
    val senderUsername: String? = null,

)
