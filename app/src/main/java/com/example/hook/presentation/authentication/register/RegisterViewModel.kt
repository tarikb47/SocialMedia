package com.example.hook.presentation.authentication.register

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.exception.FacebookSignInFailedException
import com.example.hook.common.exception.FailedToSaveUserException
import com.example.hook.common.exception.GoogleSignInFailedException
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hook.common.result.Result
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import com.example.hook.domain.usecases.remotedatabaseusecases.GetFirebaseUserUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SaveUserToFirebaseUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SignInWithFacebookUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SignInWithGoogleUseCase
import com.example.hook.presentation.authentication.helpers.InputFieldValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val inputFieldValidator: InputFieldValidator,
    private val googleSignInUseCase: SignInWithGoogleUseCase,
    private val facebookSignInUseCase: SignInWithFacebookUseCase,
    private val saveUserToFirebaseUseCase: SaveUserToFirebaseUseCase,
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase,
    private val userRepository: UserRepository,


    ) : ViewModel() {
    private val _registerState: MutableStateFlow<RegisterState> =
        MutableStateFlow(RegisterState.Initial)
    val registerState = _registerState.asStateFlow()

    fun validateUserInput(username: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = withContext(Dispatchers.IO) {
                inputFieldValidator.registrationValidator(username, email, phone, password)
            }

            val nextState = when (result) {
                is Result.Success -> RegisterState.ValidInput
                is Result.Error -> RegisterState.Error(result.error)
            }
            _registerState.value = nextState
        }
    }

    fun resetValidationState() {
        _registerState.value = RegisterState.Initial
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val googleClient = googleSignInUseCase.setupGoogleSignInClient()
        return googleClient
    }

    fun handleGoogleSignIn(data: Intent?) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerState.value = RegisterState.Loading
            googleSignInUseCase.handleGoogleSignInResult(data).flatMapLatest { result ->
                when (result) {
                    is Result.Error -> flowOf(result)
                    is Result.Success -> if (result.data) saveUserToFirebaseUseCase.saveGoogleFacebookUser() else emptyFlow()
                }
            }.collectLatest {
                handleSignUpResult(it)
            }
        }
    }

    fun handleFacebookAccessToken(token: AccessToken) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerState.value = RegisterState.Loading

            facebookSignInUseCase.handleFacebookAccessToken(token).flatMapLatest { result ->
                when (result) {
                    is Result.Error -> flowOf(result)


                    is Result.Success -> {
                        if (result.data) saveUserToFirebaseUseCase.saveGoogleFacebookUser()
                        else emptyFlow()
                    }
                }
            }.collectLatest {handleSignUpResult(it)
                }
        }
    }
    /*private suspend fun handleSignUpResult(result: Result<Unit>) {
        when (result) {
            is Result.Error -> _registerState.emit(RegisterState.Error(FailedToSaveUserException()))
            is Result.Success -> {
                val userResult = getFirebaseUserUseCase().first()
                if (userResult is Result.Success) {
                    saveUserLocally(userResult.data)
                    _registerState.emit(RegisterState.CredentialSignInSuccess)
                } else {
                    _registerState.emit(RegisterState.Error(FailedToSaveUserException()))
                }
            }
        }
    }*/
    private suspend fun handleSignUpResult(result: Result<Unit>) {
        val nextState = when (result) {
            is Result.Error -> RegisterState.Error(result.error)
            is Result.Success -> {
                val userResult = getFirebaseUserUseCase().first()
                if (userResult is Result.Success) {
                    saveUserLocally(userResult.data)
                    RegisterState.CredentialSignInSuccess
                } else {
                    RegisterState.Error(FailedToSaveUserException())
                }
            }
        }
        _registerState.emit(nextState)
    }

    private fun saveUserLocally(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserToLocal(user)
        }
    }
}