package com.example.hook.di

import android.content.Context
import androidx.room.Room
import com.example.hook.data.local.AppDatabase
import com.example.hook.data.local.ContactDatabase
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.ContactDao
import com.example.hook.data.local.dao.UserDao
import com.example.hook.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {


    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.UserDao()
    }


    @Module
    @InstallIn(SingletonComponent::class)
    object DatabaseModule {

        @Provides
        @Singleton
        fun provideContactDatabase(appContext: Context): ContactDatabase {
            return Room.databaseBuilder(
                appContext,
                ContactDatabase::class.java,
                "contact_database"
            ).build()
        }

        @Provides
        @Singleton
        fun provideContactDao(contactDatabase: ContactDatabase): ContactDao {
            return contactDatabase.contactDao()
        }
    }



}
