package com.example.hook.domain.usecases.remotedatabaseusecases

import android.util.Log
import com.example.hook.domain.repository.AuthRepository
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow

class RegisterUseCase @Inject constructor(val repository: AuthRepository) {
    suspend operator fun invoke(username:String, email: String, phoneNumber:String, password : String): Flow<Result<Unit>> {
        return repository.registerUser(username, email, phoneNumber, password)
    }
}