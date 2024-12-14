package com.example.hook.presentation.authentication.register

sealed class RegisterState {
    data object Initial : RegisterState()
    data object Loading : RegisterState()
    data object ValidInput : RegisterState()
    data object CredentialSignInSuccess : RegisterState()
    data class Error(val error: Throwable) : RegisterState()

}