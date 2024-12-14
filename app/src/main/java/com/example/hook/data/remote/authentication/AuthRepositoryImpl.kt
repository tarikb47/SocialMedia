package com.example.hook.data.remote.authentication

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.hook.common.result.Result
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
) :
    AuthRepository {
    override suspend fun registerUser(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Flow<Result<Unit>> {
        return firebaseAuthDataSource.registerUser(username, email, phoneNumber, password)
    }

    override suspend fun isUsernameTaken(username: String): Flow<Result<Boolean>> {
        return firebaseAuthDataSource.isUsernameTaken(username)
    }

    override suspend fun isEmailTaken(email: String): Flow<Result<Boolean>> {
        return firebaseAuthDataSource.isEmailTaken(email)
    }

    override suspend fun phoneSignup(credential: PhoneAuthCredential): Flow<Result<Unit>> {
        return firebaseAuthDataSource.phoneSignUp(credential)
    }

    override suspend fun isPhoneNumberTaken(phoneNumber: String): Flow<Result<Boolean>> {
        return firebaseAuthDataSource.isPhoneNumberRegistered(phoneNumber)
    }

    override suspend fun loginUser(email: String, password: String): Flow<Result<Unit>> {
        return firebaseAuthDataSource.loginUser(email, password)
    }

     override suspend fun signInWithCredential(credential: AuthCredential): Flow<Result<Boolean>> {
        return firebaseAuthDataSource.signInWithCredential(credential)
    }

    override suspend fun signInWithGoogle(credential: AuthCredential): Flow<Result<Unit>> {
    return firebaseAuthDataSource.signInWithGoogle(credential)   }

    override suspend fun signInWithFacebook(token: AccessToken): Flow<Result<Unit>> {
return firebaseAuthDataSource.signInWithFacebook(token)   }

    override suspend fun saveUserToFirestore(
        username: String?,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        userId: String
    ): Flow<Result<Boolean>> {
        return firebaseAuthDataSource.saveUserToFirestore(
            username, email, phoneNumber, firebaseToken, userId
        )
    }
    override suspend fun getCurrentUser(): Result<FirebaseUser?> {
        return firebaseAuthDataSource.getCurrentUser()
    }
    override suspend fun getFirebaseUser(): Flow<Result<User>> {
        return firebaseAuthDataSource.getFirebaseUser()
    }

    override suspend fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> {
        return firebaseAuthDataSource.sendPasswordResetEmail(email)
    }
    override suspend fun saveFacebookGoogleUser() : Flow<Result<Unit>>{
        return firebaseAuthDataSource.saveFacebookGoogleUser()
    }


}
