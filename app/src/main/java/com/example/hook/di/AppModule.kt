package com.example.hook.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.hook.data.local.AppDatabase
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.remote.authentication.AuthRepositoryImpl
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.domain.repository.AuthRepository
import com.example.hook.domain.usecase.FacebookLoginUseCase
import com.example.hook.domain.usecase.LoginUseCase
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides
    @Singleton
    fun provideFirebaseFirestore() :FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(auth: FirebaseAuth, firestore : FirebaseFirestore): FirebaseAuthDataSource =
        FirebaseAuthDataSource(auth, firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(dataSource: FirebaseAuthDataSource): AuthRepository =
        AuthRepositoryImpl(dataSource)
    @Provides
    fun provideFacebookLoginUseCase(
        authRepository: AuthRepository
    ): FacebookLoginUseCase = FacebookLoginUseCase(authRepository)


}