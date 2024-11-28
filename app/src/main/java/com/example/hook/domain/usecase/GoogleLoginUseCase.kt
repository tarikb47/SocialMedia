package com.example.hook.domain.usecase

import android.content.Intent
import com.example.hook.domain.repository.AuthRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun signInWithGoogle(credential: String): Result<Unit> {
        return authRepository.signInWithGoogle(credential)
    }
}
