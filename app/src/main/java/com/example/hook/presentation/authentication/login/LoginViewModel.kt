package com.example.hook.presentation.authentication.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.exception.BlankFieldsException
import com.example.hook.common.exception.FailedPasswordResetException
import com.example.hook.common.exception.FailedToSaveUserException
import com.example.hook.common.exception.InvalidEmailFormatException
import com.example.hook.common.exception.UnregisteredUserException
import com.example.hook.common.result.Result
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import com.example.hook.domain.usecases.remotedatabaseusecases.ForgotPasswordUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.GetFirebaseUserUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.LoginUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SaveUserToFirebaseUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SignInWithFacebookUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SignInWithGoogleUseCase
import com.example.hook.presentation.authentication.helpers.InputFieldValidator
import com.example.hook.presentation.authentication.register.RegisterState

import com.facebook.AccessToken
import com.facebook.login.Login
import com.google.android.gms.auth.api.signin.GoogleSignInClient

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: SignInWithGoogleUseCase,
    private val userRepository: UserRepository,
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val facebookSignInUseCase: SignInWithFacebookUseCase,
    private val inputFieldValidator: InputFieldValidator,
    private val saveUserToFirebaseUseCase: SaveUserToFirebaseUseCase


) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun getGoogleSignInClient(): GoogleSignInClient {
        val googleClient = googleSignInUseCase.setupGoogleSignInClient()
        return googleClient
    }

    fun handleGoogleSignIn(data: Intent?) = viewModelScope.launch(Dispatchers.IO) {
        googleSignInUseCase.handleGoogleSignInResult(data).collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    if (saveUserToFirestore()) _loginState.emit(LoginState.Success)
                    else _loginState.emit(LoginState.Error(FailedToSaveUserException()))
                }

                is Result.Error -> _loginState.emit(LoginState.Error(result.error))
            }
        }
    }

    fun handleFacebookAccessToken(token: AccessToken) = viewModelScope.launch(Dispatchers.IO) {
        facebookSignInUseCase.handleFacebookAccessToken(token).collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    if (saveUserToFirestore()) { _loginState.emit(LoginState.Success)
                    } else {
                        _loginState.value = LoginState.Error(FailedToSaveUserException())
                    }
                }
                is Result.Error -> _loginState.emit(LoginState.Error(FailedToSaveUserException()))
            }

        }
    }

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        if (!inputFieldValidator.isValidEmail(email)) {
            _loginState.value = LoginState.Error(InvalidEmailFormatException())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            loginUseCase(email, password).collectLatest { result ->
                when (result) {
                    is Result.Error -> _loginState.value = LoginState.Error(result.error)

                    is Result.Success -> fetchAndSaveFirebaseUser()
                }
            }

        }
    }

    private fun saveUserLocally(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserToLocal(user)
        }
    }

    private suspend fun saveUserToFirestore(): Boolean {
        return when (saveUserToFirebaseUseCase.saveGoogleFacebookUser().first()) {
            is Result.Error -> false
            is Result.Success -> {
                val userResult = getFirebaseUserUseCase().first()
                if (userResult is Result.Success) {
                    saveUserLocally(userResult.data)
                    return true
                } else {
                    return false
                }
            }
        }
    }

    private fun fetchAndSaveFirebaseUser() = viewModelScope.launch(Dispatchers.IO) {
        getFirebaseUserUseCase().collectLatest { result ->
            when (result) {
                is Result.Error -> {
                    _loginState.emit(LoginState.Error(FailedToSaveUserException()))
                }
                is Result.Success -> {
                    saveUserLocally(result.data)
                    _loginState.emit(LoginState.Success)
                }
            }
        }

    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _loginState.value = LoginState.Error(BlankFieldsException())
            return
        } else if (!inputFieldValidator.isValidEmail(email)) {
            _loginState.value = LoginState.Error(InvalidEmailFormatException())
            return
        }
        _loginState.value = LoginState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            forgotPasswordUseCase(email).collectLatest { result ->
                when (result) {
                    is Result.Error -> _loginState.emit(
                        LoginState.Error(
                            UnregisteredUserException()
                        )
                    )

                    is Result.Success -> _loginState.value = LoginState.EmailSent
                }
            }

        }
    }


}




