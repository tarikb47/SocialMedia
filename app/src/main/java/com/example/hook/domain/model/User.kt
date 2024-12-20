package com.example.hook.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val id : String,
    val username: String?,
    val email: String?,
    val phoneNumber: String?,
    val firebaseToken : String?,
    val photoUrl: String?
) : Parcelable
