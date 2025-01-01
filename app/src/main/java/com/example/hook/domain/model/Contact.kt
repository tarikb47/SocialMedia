package com.example.hook.domain.model

import com.google.firebase.Timestamp


data class Contact(
val userId: String,
val username: String?,
val phoneNumber: String?,
val email: String?,
val photoUrl: String? = null,
    )
