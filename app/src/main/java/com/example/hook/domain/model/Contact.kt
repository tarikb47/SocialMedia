package com.example.hook.domain.model

    data class Contact(
val userId: String,
val username: String?,
val phoneNumber: String?,
val email: String?,
val photoUrl: String? = null
)

