package com.example.hook.presentation.authentication.phoneVerification

sealed class RegisterState {
    object Idle : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}