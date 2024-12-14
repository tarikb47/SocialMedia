package com.example.hook.di

import com.example.hook.data.remote.authentication.AuthRepositoryImpl
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.domain.repository.AuthRepository
import com.example.hook.domain.repository.UserRepository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideFirebaseAuthDataSource(auth: FirebaseAuth, firestore : FirebaseFirestore , userRepository: UserRepository): FirebaseAuthDataSource =
        FirebaseAuthDataSource(auth, firestore, userRepository)

    @Provides
    @Singleton
    fun provideAuthRepository(dataSource: FirebaseAuthDataSource): AuthRepository =
        AuthRepositoryImpl(dataSource)
   /* @Provides
    fun provideFacebookLoginUseCase(
        authRepository: AuthRepository
    ): FacebookLoginUseCase = FacebookLoginUseCase(authRepository)
*/

}