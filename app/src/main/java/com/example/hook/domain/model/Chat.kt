package com.example.hook.domain.model

data class Chat(
    var chatId: String = "",
    var users: List<String> = emptyList(),
    var lastMessage: String = "",
    var timestamp: Long = 0L,
    var username: String? = null,
    var photoUrl: String? = null
)