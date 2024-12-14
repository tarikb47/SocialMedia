package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.common.result.Result
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFirebaseUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<Result<User>> {
    return authRepository.getFirebaseUser()
    }
}