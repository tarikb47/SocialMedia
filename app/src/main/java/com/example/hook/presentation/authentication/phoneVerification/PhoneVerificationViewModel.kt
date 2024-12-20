package com.example.hook.presentation.authentication.phoneVerification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.exception.RegistrationFailedException
import com.example.hook.common.exception.VerificationFailedException
import com.example.hook.domain.usecases.remotedatabaseusecases.RegisterUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.SendPhoneVerificationUseCase
import com.example.hook.domain.usecases.remotedatabaseusecases.PhoneSignupCodeUseCase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hook.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel
class PhoneVerificationViewModel @Inject constructor(
    private val sendPhoneVerificationUseCase: SendPhoneVerificationUseCase,
    private val phoneSignupCodeUseCase: PhoneSignupCodeUseCase,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    fun sendVerificationCode(phoneNumber: String) =
        viewModelScope.launch(Dispatchers.IO) {
            sendPhoneVerificationUseCase(
                phoneNumber,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        _verificationState.value = VerificationState.CodeSent(verificationId)
                    }

                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        _verificationState.value = VerificationState.CompletedVerification
                        phoneSignup(credential)

                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _verificationState.value =
                            VerificationState.Error(e)
                    }
                })

        }


    private fun phoneSignup(credential: PhoneAuthCredential) =
        viewModelScope.launch(Dispatchers.IO) {
            phoneSignupCodeUseCase(credential).collectLatest { result ->
                when (result) {
                    is Result.Success -> _verificationState.value =
                        VerificationState.SignedInWithPhoneNumber

                    is Result.Error -> _verificationState.value =
                        VerificationState.Error(VerificationFailedException())
                }
            }
        }


    fun phoneSignUWithCode(verificationId: String, code: String) {
        _verificationState.value = VerificationState.Loading
        viewModelScope.launch {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            phoneSignup(credential)
        }
    }

    fun registerUser(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (_verificationState.value != VerificationState.SignedInWithPhoneNumber) {
            _verificationState.value = VerificationState.Error(VerificationFailedException())
            return@launch
        }

        registerUseCase(username, email, phoneNumber, password).collectLatest { result ->
            when (result) {
                is Result.Success -> _verificationState.emit(VerificationState.UserRegistered)
                is Result.Error -> _verificationState.emit(
                    VerificationState.Error(
                        RegistrationFailedException()
                    )
                )
            }
        }
    }

}

