package com.example.hook.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class SplashViewModel : ViewModel() {
    private val _splashState = MutableStateFlow(false)
    val splashState: StateFlow<Boolean> get() = _splashState

    fun initializeApp() {
        viewModelScope.launch {
            delay(3000)
            _splashState.value = true
        }
    }
}