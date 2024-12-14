package com.example.hook.presentation.authentication.phoneVerification


sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class CodeSent(val verificationId: String) : VerificationState()
    object CompletedVerification : VerificationState()
    object SignedInWithPhoneNumber : VerificationState()
    object UserRegistered : VerificationState()
    data class Error(val error: Throwable) : VerificationState()

}
