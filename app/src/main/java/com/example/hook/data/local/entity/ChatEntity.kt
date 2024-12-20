package com.example.hook.data.local.entity

data class ChatEntity(
    val chatId: String,
    val members: List<String>,
    val lastMessage: String?,
    val lastMessageTime: Long?,
)