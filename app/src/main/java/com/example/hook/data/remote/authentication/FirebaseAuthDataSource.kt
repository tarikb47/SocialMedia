package com.example.hook.data.remote.authentication

import android.content.Intent
import android.util.Log
import com.facebook.AccessToken
import com.facebook.internal.BoltsMeasurementEventListener
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,


    ) {

    suspend fun registerUser(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("No authenticated user to link credentials"))
            val emailCredential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            currentUser.linkWithCredential(emailCredential).await()
            val userId = currentUser.uid
            val userData = hashMapOf(
                "username" to username,
                "phoneNumber" to phoneNumber,
                "email" to email
            )
            try {
                firestore.collection("users")
                    .document(userId)
                    .set(userData)
                    .await()
                Result.success(Unit)
            } catch (firestoreException: Exception) {
                Result.failure(Exception("Failed to save user data to Firestore: ${firestoreException.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isEmailTaken(email: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isPhoneNumberRegistered(phoneNumber: String): Boolean {
        return try {
            val snapshot =
                firestore.collection("users").whereEqualTo("phoneNumber", phoneNumber).get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }

    }


    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(validPhoneNumberPattern)
    }

    fun isValidPassword(password: String): Boolean {
        return password.matches(validPasswordPattern)
    }

    companion object {
        private val validPasswordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$")
        private val validPhoneNumberPattern = Regex("^\\+\\d{7,15}$")

    }

    fun getFacebookAuthCredential(token: AccessToken): AuthCredential {
        return com.google.firebase.auth.FacebookAuthProvider.getCredential(token.token)
    }

    suspend fun signInWithFacebook(credential: AuthCredential): Result<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun signInWithGoogle(credential: String): Result<Unit> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential, null)
            auth.signInWithCredential(firebaseCredential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun phoneVerification(credential: AuthCredential): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.linkWithCredential(credential).await()
            } else {
                auth.signInWithCredential(credential).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FirebaseAuthDataSource", "phoneVerification failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}