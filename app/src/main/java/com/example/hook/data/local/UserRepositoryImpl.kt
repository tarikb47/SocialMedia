package com.example.hook.data.local

import com.example.hook.common.result.Result
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val userDao: UserDao) : UserRepository {
    override suspend fun saveUserToLocal(user: User) {
        val userEntity = UserEntity(
            id = 1,
            username = user.username,
            email = user.email,
            phoneNumber = user.phoneNumber,
            firebaseToken = user.firebaseToken
        )
        userDao.insertUser(userEntity)
    }
    override fun updateToken(firebaseToken: String) {
        val existingUser = userDao.getUserById(1)
        userDao.insertUser(existingUser.copy(firebaseToken = firebaseToken))
    }
    override suspend fun getUserFromLocal(userId: Int): User  {
        val userEntity = userDao.getUserById(userId)
        return userEntity.let {
          User(
                username = it.username,
                email = it.email,
                phoneNumber = it.phoneNumber,
                firebaseToken = it.firebaseToken
            )
        }
    }
    override suspend fun clearLocalUserData() {
        userDao.clearAllUsers()
    }
    override suspend fun deleteUser(userId: Int) {
        userDao.deleteUser(userId)
    }


}