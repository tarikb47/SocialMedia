package com.example.hook.di

import android.content.Context
import androidx.room.Room
import com.example.hook.data.local.AppDatabase
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

}
