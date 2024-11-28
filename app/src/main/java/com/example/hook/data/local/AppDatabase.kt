package com.example.hook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.domain.model.User

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao() : UserDao
}