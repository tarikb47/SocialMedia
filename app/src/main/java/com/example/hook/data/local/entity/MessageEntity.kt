package com.example.hook.data.local.entity

import com.example.hook.common.enums.MessageStatus
import com.example.hook.common.enums.MessageType

data class MessageEntity(
    val messageId: String,
    val senderId: String,
    val chatId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val status: MessageStatus
    )
