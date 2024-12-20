package com.example.hook.domain.repository

import com.example.hook.common.result.Result
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.domain.model.User
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

interface UserRepository {
    suspend fun saveUserToLocal(user: User)
    suspend fun getUserFromLocal(userId: Int): User
    suspend fun clearLocalUserData()
    fun updateToken(firebaseToken: String)
    suspend fun deleteUser(userId: Int)
    suspend fun getUserEntityFromLocal() : UserEntity
}
