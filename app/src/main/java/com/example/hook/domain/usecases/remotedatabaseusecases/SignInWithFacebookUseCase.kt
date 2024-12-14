package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.domain.repository.AuthRepository
import com.facebook.AccessToken
import javax.inject.Inject
import com.example.hook.common.result.Result
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.flow.Flow

class SignInWithFacebookUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun handleFacebookAccessToken(token: AccessToken): Flow<Result<Boolean>> {
        val credential = FacebookAuthProvider.getCredential(token.token)
        return authRepository.signInWithCredential(credential)
    }
}
