package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.common.exception.EmailTakenException
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class ForgotPasswordUseCase @Inject constructor(
    private val repository: FirebaseAuthDataSource
) {
    suspend operator fun invoke(email: String):
            Flow<Result<Unit>> {
        val isTaken = repository.isEmailTaken(email).first()
        return if (isTaken is Result.Success && isTaken.data) {
            repository.sendPasswordResetEmail(email)
        } else {
            flowOf(Result.Error(EmailTakenException()))
        }
    }
}