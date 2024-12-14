package com.example.hook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey  val id: Int = 1,
    val username: String? = "tarik",
    val email: String? = "tarik@gmaiil.com",
    val phoneNumber: String? = "+38761068883",
    val firebaseToken: String? = null
)
