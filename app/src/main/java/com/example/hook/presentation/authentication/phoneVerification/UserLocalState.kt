package com.example.hook.presentation.authentication.phoneVerification

import com.example.hook.domain.model.User

sealed class UserLocalState {
    object Idle : UserLocalState()
    object Loading : UserLocalState()
    data class Success(val user: User) : UserLocalState()
    data class Error(val message: String) : UserLocalState()
}