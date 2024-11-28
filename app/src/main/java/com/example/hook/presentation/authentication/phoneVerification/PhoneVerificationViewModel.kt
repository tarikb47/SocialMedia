package com.example.hook.presentation.authentication.phoneVerification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import com.example.hook.domain.repository.UserRepository
import com.example.hook.domain.usecase.RegisterUseCase
import com.example.hook.domain.usecase.SendPhoneVerificationUseCase
import com.example.hook.domain.usecase.VerifyPhoneCodeUseCase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneVerificationViewModel @Inject constructor(
    private val sendPhoneVerificationUseCase: SendPhoneVerificationUseCase,
    private val verifyPhoneCodeUseCase: VerifyPhoneCodeUseCase,
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    private var fetchedUser: User? = null
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState
    private val _registrationState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registrationState: StateFlow<RegisterState> = _registrationState
    private val _userLocalFetchStateFlow = MutableStateFlow<UserLocalState>(UserLocalState.Idle)
    val userLocalFetchStateFlow: StateFlow<UserLocalState> = _userLocalFetchStateFlow

    fun sendVerificationCode(phoneNumber: String) {
        _verificationState.value = VerificationState.Loading
        viewModelScope.launch {
            try {
                sendPhoneVerificationUseCase(
                    phoneNumber,
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Log.d("Tarik", "sendalo")

                            _verificationState.value = VerificationState.CodeSent(verificationId)
                        }
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            Log.d("Tarik", "completana")
                            _verificationState.value = VerificationState.Completed
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Log.d("Tarik", "feilalo")

                            _verificationState.value =
                                VerificationState.Error(e.message ?: "Verification failed")
                        }

                        // provjeritie state koji baca gdje
                        // meni je s tobom lila lila sve

                    })
            } catch (e: Exception) {
                _verificationState.value =
                    VerificationState.Error(e.message ?: "Failed to send code")
            }
        }
    }

    fun verifyCode(verificationId: String, code: String, user: User) {
        viewModelScope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                val result = verifyPhoneCodeUseCase(credential)
                if (result.isSuccess) {
                    _verificationState.value = VerificationState.Completed
                    //registerUser(user)
                } else {
                    _verificationState.value = VerificationState.Error("Verification failed")
                }
            } catch (e: Exception) {
                _verificationState.value =
                    VerificationState.Error(e.message ?: "Verification failed")
            }
        }
    }

    fun registerUser(user: User) {
        viewModelScope.launch {
            val result = registerUseCase(user)
            _registrationState.value = if (result.isSuccess) {
                RegisterState.Success
            } else {
                RegisterState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun getUserFromLocal(userId: Int) {
        viewModelScope.launch {
            _userLocalFetchStateFlow.value = UserLocalState.Loading
            val user = userRepository.getUserFromLocal(userId)
            if (user != null) {
                fetchedUser = user
                _userLocalFetchStateFlow.value = UserLocalState.Success(user)
            } else {
                _userLocalFetchStateFlow.value = UserLocalState.Error("User not found")
            }

        }
    }

    fun getFetchedUser() = fetchedUser
}