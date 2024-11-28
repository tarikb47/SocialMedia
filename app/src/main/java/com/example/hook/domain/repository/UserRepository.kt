package com.example.hook.domain.repository

import com.example.hook.domain.model.User
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

interface UserRepository{
    suspend fun saveUserToLocal(user: User)

    suspend fun getUserFromLocal(userId:Int):User?
    suspend fun clearLocalUserData()

    suspend fun deleteUser(userId: Int)
}
