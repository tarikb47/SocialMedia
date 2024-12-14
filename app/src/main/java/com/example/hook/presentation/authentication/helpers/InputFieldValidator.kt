package com.example.hook.presentation.authentication.helpers

import android.util.Log
import com.example.hook.common.exception.BlankFieldsException
import com.example.hook.common.exception.EmailTakenException
import com.example.hook.common.exception.InvalidEmailFormatException
import com.example.hook.common.exception.InvalidPasswordException
import com.example.hook.common.exception.InvalidPhoneNumberException
import com.example.hook.common.exception.PhoneNumberRegisteredException
import com.example.hook.common.exception.UsernameTakenException
import com.example.hook.common.exception.WeakPasswordException
import com.example.hook.domain.repository.AuthRepository
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class InputFieldValidator @Inject constructor(private val auth: AuthRepository) {

    suspend fun registrationValidator(
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<Unit> {
        return when {
            email.isBlank() || password.isBlank() || username.isBlank() || phone.isBlank() -> Result.Error(
                BlankFieldsException()
            )
            password.length < 6 -> Result.Error(InvalidPasswordException())
            !isValidEmail(email) -> Result.Error(InvalidEmailFormatException())
            !isValidPhoneNumber(phone) -> Result.Error(InvalidPhoneNumberException())
            !isValidPassword(password) -> Result.Error(WeakPasswordException())
            isUsernameRegistered(username) -> Result.Error(UsernameTakenException())
            isEmailRegistered(email) -> Result.Error(EmailTakenException())
            isPhoneNumberRegistered(phone) -> Result.Error(
                PhoneNumberRegisteredException()
            )

            else -> Result.Success(Unit)
        }
    }

    private suspend fun isUsernameRegistered(username: String): Boolean {
        return when (val result = auth.isUsernameTaken(username).first()) {
            is Result.Error -> error("Unexpected")
            is Result.Success -> result.data
        }
    }


    private suspend fun isEmailRegistered(email: String): Boolean {
        return when (val result = auth.isEmailTaken(email).first()) {
            is Result.Error -> error("Unexpected")
            is Result.Success -> result.data
        }
    }



    private suspend fun isPhoneNumberRegistered(phoneNumber: String): Boolean {
        return when (val result = auth.isPhoneNumberTaken(phoneNumber).first()) {
            is Result.Error -> error("Unexpected")
            is Result.Success -> result.data
        }
    }




    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(VALID_PHONE_NUMBER_PATTERN)
    }

    fun isValidEmail(email: String): Boolean {
        return email.matches(VALID_EMAIL_PATTERN)
    }

    private fun isValidPassword(password: String): Boolean {
        return password.matches(VALID_PASSWORD_PATTERN)
    }


    companion object {
        private val VALID_EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        private val VALID_PASSWORD_PATTERN = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$")
        private val VALID_PHONE_NUMBER_PATTERN = Regex("^\\+\\d{7,15}$")
    }
}
