package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow


class SignInWIthCredentialUseCase @Inject constructor(private val repository: AuthRepository){
    suspend fun signInWithCredential(credential: AuthCredential): Flow<Result<Boolean>> {
        return repository.signInWithCredential(credential)
    }
}