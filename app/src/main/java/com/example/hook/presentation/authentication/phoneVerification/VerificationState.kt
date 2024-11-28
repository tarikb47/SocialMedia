package com.example.hook.presentation.authentication.phoneVerification


sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class CodeSent(val verificationId: String) : VerificationState()
    object Completed : VerificationState()
    data class Error(val message: String) : VerificationState()
}
