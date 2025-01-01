package com.example.hook.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.remote.home.contacts.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
   private val db : UserRepositoryImpl,
   ) : ViewModel() {
    private val _splashState = MutableStateFlow<SplashState>(SplashState.Idle)
    val splashState: StateFlow<SplashState> get() = _splashState

    fun initializeApp() {
        viewModelScope.launch {
            delay(3000)
            userSession()
        }
    }
    private fun userSession(){
        viewModelScope.launch(Dispatchers.IO) {
            val userExistence =db.getUserEntityFromLocal()
            Log.d("Tarik", "userSession: $userExistence")
            if (userExistence == null) {
                _splashState.value = SplashState.UserNotLoggedIn
            }
            else _splashState.value = SplashState.UserLoggedIn
        }
    }

}