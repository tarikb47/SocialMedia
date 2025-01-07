package com.example.hook.domain.model

data class Message (
    val id : String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0
)
