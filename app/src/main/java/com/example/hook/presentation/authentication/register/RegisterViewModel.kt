package com.example.hook.presentation.authentication.register

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import com.example.hook.domain.usecase.FacebookLoginUseCase
import com.example.hook.domain.usecase.GoogleLoginUseCase
import com.example.hook.domain.usecase.RegisterUseCase
import com.example.hook.domain.usecase.ValidateInputFields
import com.example.hook.presentation.authentication.phoneVerification.RegisterState
import com.facebook.AccessToken
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateInputFields: ValidateInputFields,
    private val facebookLoginUseCase: FacebookLoginUseCase,
    private val userRepository: UserRepository,
    private val googleSignInUseCase: GoogleLoginUseCase


) :
    ViewModel() {
    val validationState = MutableStateFlow<ValidationResult>(ValidationResult.Idle)
    val facebookLoginState = MutableStateFlow<FacebookLoginResult>(FacebookLoginResult.Idle)
    private val _googleSignInState = MutableStateFlow<GoogleSignInResult>(GoogleSignInResult.Idle)
    val googleSignInState: StateFlow<GoogleSignInResult> = _googleSignInState
    fun validateUserInput(username: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            val user = User(username, email, phone, password)
            validationState.value = validateInputFields(user)
        }
    }

    fun resetValidationState() {
        validationState.value = ValidationResult.Idle
    }

    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            facebookLoginState.value = FacebookLoginResult.Loading
            val result = facebookLoginUseCase(token)
            facebookLoginState.value = if (result.isSuccess) {
                FacebookLoginResult.Success
            } else {
                FacebookLoginResult.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(credential: String) {
        viewModelScope.launch {
            _googleSignInState.value = GoogleSignInResult.Loading
            val result = googleSignInUseCase.signInWithGoogle(credential)
            _googleSignInState.value = if (result.isSuccess) {
                GoogleSignInResult.Success
            } else {
                GoogleSignInResult.Error(result.exceptionOrNull()?.message ?: "Sign-in failed")
            }
        }
    }

    fun saveUserLocally(user: User) {
        viewModelScope.launch {
            userRepository.saveUserToLocal(user)
        }
    }
}

sealed class GoogleSignInResult {
    object Idle : GoogleSignInResult()
    object Loading : GoogleSignInResult()
    object Success : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

sealed class FacebookLoginResult {
    object Idle : FacebookLoginResult()
    object Loading : FacebookLoginResult()
    object Success : FacebookLoginResult()
    data class Error(val message: String) : FacebookLoginResult()
}

