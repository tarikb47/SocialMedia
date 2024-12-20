package com.example.hook.presentation.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.domain.repository.UserRepository
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState


     fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepo.getUserEntityFromLocal()
            _userState.value = user
        }
    }
}