package com.example.hook.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val username: String?,
    val email: String?,
    val phoneNumber: String?,
    val firebaseToken : String?
) : Parcelable
