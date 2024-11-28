package com.example.hook.domain.usecase

import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(val repository: AuthRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return repository.registerUser(user)

    }
}