package com.example.hook.presentation.home.contacts.addContact

sealed class ContactState {
    object Idle : ContactState()
    object Loading : ContactState()
    object Success : ContactState()
    data class Error(val message: Throwable) : ContactState()
    data class InputError(val message: Throwable) : ContactState()
}
