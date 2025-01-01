package com.example.hook.presentation.splash

sealed class SplashState {
    object Idle : SplashState()
    object UserLoggedIn : SplashState()
    object UserNotLoggedIn : SplashState()
}
