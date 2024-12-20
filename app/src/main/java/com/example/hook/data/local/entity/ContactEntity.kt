package com.example.hook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val userId: String,
    val username: String?,
    val phoneNumber: String?,
    val email: String?,
    val photoUrl: String?)