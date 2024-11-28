package com.example.hook.domain.usecase

import com.example.hook.domain.repository.AuthRepository
import com.facebook.AccessToken
import javax.inject.Inject

class FacebookLoginUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(token: AccessToken): Result<Unit> {
        return repository.signInWithFacebook(token)
    }
}