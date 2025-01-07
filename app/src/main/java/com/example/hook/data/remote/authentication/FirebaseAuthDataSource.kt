package com.example.hook.data.remote.authentication

import android.net.Uri
import com.example.hook.common.enums.FieldType
import com.example.hook.common.exception.FailedToSaveUserException
import com.example.hook.common.exception.IncorrectPasswordException
import com.example.hook.common.exception.NoContactFoundException
import com.example.hook.common.exception.UnexpectedErrorException
import com.example.hook.common.exception.UnregisteredUserException
import com.example.hook.common.exception.UserNotLoggedInException
import com.example.hook.common.ext.asFlow
import com.example.hook.common.ext.mapError
import com.example.hook.common.ext.mapSuccess
import com.example.hook.common.result.Result
import com.example.hook.domain.model.Contact
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import com.facebook.AccessToken
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Year
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {
    fun registerUser(
        username: String, email: String, phoneNumber: String, password: String
    ): Flow<Result<Unit>> {
        return when (val currentUserResult = getCurrentUser()) {
            is Result.Success -> {
                val currentUser = currentUserResult.data
                getIdToken(currentUser).flatMapLatest { tokenResult ->
                    when (tokenResult) {
                        is Result.Success -> {
                            val token = tokenResult.data
                            linkEmailCredential(
                                currentUser, EmailAuthProvider.getCredential(email, password)
                            ).flatMapLatest { linkResult ->
                                when (linkResult) {
                                    is Result.Success -> {
                                        saveUserToFirestore(
                                            username = username,
                                            email = email,
                                            phoneNumber = phoneNumber,
                                            firebaseToken = token,
                                            userId = currentUser.uid
                                        ).mapSuccess { }
                                    }

                                    is Result.Error -> flowOf(Result.Error(linkResult.error))
                                }
                            }
                        }

                        is Result.Error -> flowOf(Result.Error(tokenResult.error))
                    }
                }
            }

            is Result.Error -> flowOf(Result.Error(UserNotLoggedInException()))
        }
    }


    private fun linkEmailCredential(
        currentUser: FirebaseUser, emailCredential: AuthCredential
    ): Flow<Result<AuthResult>> = currentUser.linkWithCredential(emailCredential).asFlow()

    fun getIdToken(currentUser: FirebaseUser?): Flow<Result<String>> {
        return currentUser?.getIdToken(true)?.asFlow()?.mapSuccess { it.token.orEmpty() } ?: flowOf(
            Result.Error(UserNotLoggedInException())
        )
    }

    fun uploadUserPhoto(userId: String, photoUri: Uri): Flow<Result<String>> {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("userPhotos/$userId/profile.jpg")

        return storageRef.putFile(photoUri).asFlow()
            .flatMapLatest {
                storageRef.downloadUrl.asFlow()
            }
            .mapSuccess { uri -> uri.toString() }
            .flatMapLatest { photoUrl ->
                firestore.collection(USERS).document(userId).update(PHOTO_URL, photoUrl).asFlow()
                    .mapSuccess { photoUrl.toString() }
            }
    }

    fun saveUserToFirestore(
        username: String?,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        userId: String,
        photoUrl: String? = "https://firebasestorage.googleapis.com/v0/b/hook-c47b9.firebasestorage.app/o/Izvor%20nade.jpg?alt=media&token=96c4db7b-03db-402b-854f-05644932f5da"
    ): Flow<Result<Boolean>> = firestore.collection(USERS).document(userId).set(
        hashMapOf(
            USERNAME to username,
            EMAIL to email,
            PHONE_NUMBER to phoneNumber,
            FIREBASE_TOKEN to firebaseToken,
            PHOTO_URL to photoUrl
        )
    ).asFlow().mapSuccess { true }

    fun saveFacebookGoogleUser(): Flow<Result<Unit>> =
        when (val currentUserResult = getCurrentUser()) {
            is Result.Success -> {
                val currentUser = currentUserResult.data
                getIdToken(currentUser).flatMapLatest { tokenResult ->
                    when (tokenResult) {
                        is Result.Success -> {
                            getFirebaseUser().flatMapLatest { firebaseUserResult ->
                                when (firebaseUserResult) {
                                    is Result.Success -> {
                                        val existingUser = firebaseUserResult.data
                                        val mergedUsername =
                                            existingUser.username ?: currentUser.displayName
                                        val mergedPhoneNumber = existingUser.phoneNumber
                                            ?: currentUser.phoneNumber
                                        val mergedEmail = currentUser.email
                                        saveUserToFirestore(
                                            username = mergedUsername,
                                            email = mergedEmail,
                                            phoneNumber = mergedPhoneNumber,
                                            firebaseToken = tokenResult.data,
                                            userId = currentUser.uid
                                        ).mapSuccess { Unit }
                                            .mapError { FailedToSaveUserException() }
                                    }

                                    is Result.Error -> flowOf(Result.Error(firebaseUserResult.error))
                                }
                            }
                        }

                        is Result.Error -> flowOf(Result.Error(UserNotLoggedInException()))
                    }
                }
            }

            is Result.Error -> flowOf(Result.Error(UserNotLoggedInException()))
        }


    fun isUsernameTaken(username: String): Flow<Result<Boolean>> =
        firestore.collection(USERS).whereEqualTo(USERNAME, username).get().asFlow()
            .mapSuccess { !it.isEmpty }

    fun isEmailTaken(email: String): Flow<Result<Boolean>> =
        firestore.collection(USERS).whereEqualTo(EMAIL, email).get().asFlow()
            .mapSuccess { !it.isEmpty }

    fun isPhoneNumberRegistered(
        phoneNumber: String
    ): Flow<Result<Boolean>> =
        firestore.collection(USERS).whereEqualTo(PHONE_NUMBER, phoneNumber).get().asFlow()
            .mapSuccess { !it.isEmpty }

    fun phoneSignUp(credential: AuthCredential): Flow<Result<Unit>> =
        FirebaseAuth.getInstance().signInWithCredential(credential).asFlow().mapSuccess { }

    fun loginUser(email: String, password: String): Flow<Result<Unit>> =
        isEmailTaken(email).flatMapLatest { emailCheckResult ->
            when (emailCheckResult) {
                is Result.Success -> {
                    if (emailCheckResult.data) {
                        auth.signInWithEmailAndPassword(email, password).asFlow()
                            .mapSuccess { Unit }.mapError { error ->
                                when (error) {
                                    is FirebaseAuthInvalidCredentialsException -> IncorrectPasswordException()
                                    else -> UnexpectedErrorException()
                                }
                            }
                    } else {
                        flowOf(Result.Error(UnregisteredUserException()))
                    }
                }

                is Result.Error -> flowOf(Result.Error(emailCheckResult.error))
            }
        }

    fun getFirebaseUser(): Flow<Result<User>> = when (val currentUserResult = getCurrentUser()) {
        is Result.Success -> {
            firestore.collection(USERS).document(currentUserResult.data.uid).get().asFlow()
                .mapSuccess { documentSnapshot ->
                    User(
                        username = documentSnapshot.getString(USERNAME),
                        email = documentSnapshot.getString(EMAIL),
                        phoneNumber = documentSnapshot.getString(PHONE_NUMBER),
                        firebaseToken = documentSnapshot.getString(FIREBASE_TOKEN),
                        photoUrl = documentSnapshot.getString(PHOTO_URL),
                        id = currentUserResult.data.uid

                    )
                }
        }

        is Result.Error -> flowOf(Result.Error(UserNotLoggedInException()))
    }

    fun getContactByField(fieldValue: String, fieldType: FieldType): Flow<Result<Contact>> {
        val fieldName: String = when (fieldType) {
            FieldType.USERNAME -> USERNAME
            FieldType.EMAIL -> EMAIL
            FieldType.PHONE_NUMBER -> PHONE_NUMBER

        }

        return firestore.collection(USERS)
            .whereEqualTo(fieldName, fieldValue)
            .get()
            .asFlow()
            .map { result ->
                when (result) {
                    is Result.Success -> {
                        val document = result.data.documents.firstOrNull()
                        if (document == null) {
                            Result.Error(NoContactFoundException())
                        } else {
                            Result.Success(
                                Contact(
                                    userId = document.id,
                                    username = document.getString(USERNAME),
                                    email = document.getString(EMAIL),
                                    phoneNumber = document.getString(PHONE_NUMBER),
                                    photoUrl = document.getString(PHOTO_URL),
                                )
                            )
                        }
                    }

                    is Result.Error -> Result.Error(result.error)
                }
            }
    }

    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> =
        auth.sendPasswordResetEmail(email).asFlow().mapSuccess { (Unit) }

    fun signInWithCredential(credential: AuthCredential): Flow<Result<Boolean>> =
        auth.signInWithCredential(credential).asFlow().mapSuccess { (true) }

    fun signInWithGoogle(credential: AuthCredential): Flow<Result<Unit>> =
        auth.signInWithCredential(credential).asFlow().mapSuccess { (Unit) }

    fun signInWithFacebook(token: AccessToken): Flow<Result<Unit>> =
        auth.signInWithCredential(FacebookAuthProvider.getCredential(token.token)).asFlow()
            .mapSuccess { (Unit) }

    fun getCurrentUser(): Result<FirebaseUser> {
        return auth.currentUser?.let {
            Result.Success(it)
        } ?: Result.Error(UserNotLoggedInException())
    }
    fun uploadProfilePicture(uri: Uri): Flow<Result<Unit>> {
        val userId = auth.currentUser?.uid ?: return flowOf(Result.Error(UserNotLoggedInException()))
        val storageRef = FirebaseStorage.getInstance().reference
            .child("userPhotos/$userId/profile.jpg")
        return storageRef.putFile(uri).asFlow()
            .flatMapLatest {
                storageRef.downloadUrl.asFlow()
            }
            .mapSuccess { downloadUrl ->
                firestore.collection(USERS).document(userId).update(PHOTO_URL, downloadUrl.toString()).asFlow()
            }
            .mapSuccess { Unit }
            .mapError { error -> error }
    }



    companion object {
        private const val USERS = "Users"
        private const val USERNAME = "username"
        private const val PHONE_NUMBER = "phoneNumber"
        private const val FIREBASE_TOKEN = "firebaseToken"
        private const val EMAIL = "email"
        private const val PHOTO_URL = "photoUrl"
    }

}
