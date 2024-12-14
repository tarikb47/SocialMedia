package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.domain.repository.AuthRepository
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow


class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Flow<Result<Unit>> {
        return authRepository.loginUser(email, password)
    }
}