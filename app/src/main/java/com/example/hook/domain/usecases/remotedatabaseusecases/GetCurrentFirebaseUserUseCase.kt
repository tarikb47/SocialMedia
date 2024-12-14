package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import com.example.hook.common.result.Result

class GetCurrentFirebaseUserUseCase @Inject constructor(
    private val repository: FirebaseAuthDataSource
) {
     operator fun invoke() : Result<FirebaseUser?>{
        return repository.getCurrentUser()
    }
}