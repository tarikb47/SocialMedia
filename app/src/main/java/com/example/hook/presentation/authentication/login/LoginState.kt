package com.example.hook.presentation.authentication.login


sealed class LoginState {
    object Initial : LoginState()
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    object EmailSent : LoginState()
    object FacebookSignIn : LoginState()
    object GoogleSignIn : LoginState()
    data class Error(val message: Throwable) : LoginState()
}