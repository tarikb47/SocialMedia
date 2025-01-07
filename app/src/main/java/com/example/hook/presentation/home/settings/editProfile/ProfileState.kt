package com.example.hook.presentation.home.settings.editProfile

sealed class ProfileState {


        object Idle : ProfileState()
        object Loading : ProfileState()
        object PhotoChanged : ProfileState()
        data class Error(val message: Throwable) : ProfileState()
    }
