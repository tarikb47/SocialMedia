package com.example.hook.domain.repository

import com.example.hook.domain.model.User
import com.facebook.AccessToken
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import com.example.hook.common.result.Result
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow


interface AuthRepository {
    suspend fun registerUser(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Flow<Result<Unit>>

    suspend fun isUsernameTaken(username: String): Flow<Result<Boolean>>
    suspend fun phoneSignup(credential: PhoneAuthCredential): Flow<Result<Unit>>
    suspend fun isEmailTaken(email: String): Flow<Result<Boolean>>
    suspend fun isPhoneNumberTaken(phoneNumber: String): Flow<Result<Boolean>>
    suspend fun loginUser(email: String, password: String): Flow<Result<Unit>>
    suspend fun saveUserToFirestore(
        username: String?,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        userId: String
    ): Flow<Result<Boolean>>

    suspend fun getFirebaseUser(): Flow<Result<User>>
    suspend fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    suspend fun signInWithCredential(credential: AuthCredential): Flow<Result<Boolean>>
    suspend fun signInWithGoogle(credential: AuthCredential): Flow<Result<Unit>>
    suspend fun signInWithFacebook(token: AccessToken): Flow<Result<Unit>>
    suspend fun saveFacebookGoogleUser(): Flow<Result<Unit>>

    suspend fun getCurrentUser(): Result<FirebaseUser?>
}
