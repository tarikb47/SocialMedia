package com.example.hook.domain.usecase

import com.example.hook.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.oAuthCredential
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VerifyPhoneCodeUseCase @Inject constructor(val repository: AuthRepository) {

    suspend operator fun invoke(credential: PhoneAuthCredential): Result<Boolean> {
        return repository.phoneVerification(credential)
    }

}
