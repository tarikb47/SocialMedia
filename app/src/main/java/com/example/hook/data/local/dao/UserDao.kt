package com.example.hook.data.local.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hook.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users where id= :id")
    fun getUserById(id: Int): UserEntity

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("DELETE FROM users where id = :id")
    suspend fun deleteUser(id: Int): Int
    @Query("UPDATE users SET firebaseToken = :firebaseToken WHERE id = :id")
    suspend fun updateFirebaseToken(id: Int, firebaseToken: String)}

