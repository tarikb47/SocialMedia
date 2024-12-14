package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.domain.repository.AuthRepository
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow


class PhoneSignupCodeUseCase @Inject constructor(val repository: AuthRepository) {

    suspend operator fun invoke(credential: PhoneAuthCredential): Flow<Result<Unit>> {
        return repository.phoneSignup(credential)
    }

}
