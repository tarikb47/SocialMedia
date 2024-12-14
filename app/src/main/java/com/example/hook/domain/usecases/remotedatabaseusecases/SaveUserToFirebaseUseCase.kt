package com.example.hook.domain.usecases.remotedatabaseusecases

import com.example.hook.domain.repository.AuthRepository
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.Flow


class SaveUserToFirebaseUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun saveRegularUser(
        username: String?,
        email: String?,
        phoneNumber: String?,
        firebaseToken: String,
        userId: String
    ): Flow<Result<Boolean>> {
        return authRepository.saveUserToFirestore(
            username,
            email,
            phoneNumber,
            firebaseToken,
            userId
        )
    }
    suspend fun saveGoogleFacebookUser() : Flow<Result<Unit>> {
        return authRepository.saveFacebookGoogleUser()
    }
}






