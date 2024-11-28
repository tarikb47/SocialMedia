package com.example.hook.domain.repository

import android.content.Intent
import com.example.hook.domain.model.User
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential

interface AuthRepository {
    suspend fun  registerUser(user : User) : Result<Unit>
    suspend fun isUsernameTaken(username: String): Boolean
    fun isValidPhoneNumber(phone: String): Boolean
    fun isValidPassword(password: String): Boolean
    suspend fun signInWithFacebook(token: AccessToken): Result<Unit>
    fun getFacebookAuthCredential(token: AccessToken): AuthCredential
    suspend fun phoneVerification(credential: PhoneAuthCredential) : Result<Boolean>
    suspend fun isEmailTaken(email: String) : Boolean
    suspend fun signInWithGoogle(credential: String): Result<Unit>
    suspend fun isPhoneNumberTaken(phoneNumber : String) : Boolean
    suspend fun loginUser(email: String, password: String): Result<Unit>

}