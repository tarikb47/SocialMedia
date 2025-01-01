package com.example.hook.domain.model

import com.google.firebase.Timestamp

data class UserActivity(
    val userId: String,
    val isActive: Boolean = false,
    val lastActive: Timestamp? = null
)