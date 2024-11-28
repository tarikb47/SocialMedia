package com.example.hook.data.remote.authentication

import android.content.Intent
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val firebaseAuthDataSource: FirebaseAuthDataSource) :
    AuthRepository {
    override suspend fun registerUser(user: User): Result<Unit> {
        return firebaseAuthDataSource.registerUser(
            user.username,
            user.email,
            user.phoneNumber,
            user.password
        )
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        return firebaseAuthDataSource.isUsernameTaken(username)
    }

    override fun isValidPhoneNumber(phone: String): Boolean {
        return firebaseAuthDataSource.isValidPhoneNumber(phone)
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return firebaseAuthDataSource.isEmailTaken(email)
    }
    override fun isValidPassword(password: String): Boolean {
        return firebaseAuthDataSource.isValidPassword(password)
    }
    override suspend fun signInWithFacebook(token: AccessToken): Result<Unit> {
        val credential = firebaseAuthDataSource.getFacebookAuthCredential(token)
        return firebaseAuthDataSource.signInWithFacebook(credential)
    }

    override fun getFacebookAuthCredential(token: AccessToken) =
        firebaseAuthDataSource.getFacebookAuthCredential(token)

    override suspend fun phoneVerification(credential: PhoneAuthCredential): Result<Boolean> {
        return firebaseAuthDataSource.phoneVerification(credential)
    }

    override suspend fun signInWithGoogle(credential: String): Result<Unit> {
        return firebaseAuthDataSource.signInWithGoogle(credential)
    }

    override suspend fun isPhoneNumberTaken(phoneNumber: String): Boolean {
        return firebaseAuthDataSource.isPhoneNumberRegistered(phoneNumber)
    }
    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return firebaseAuthDataSource.loginUser(email, password)
    }
}
