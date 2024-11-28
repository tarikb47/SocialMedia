package com.example.hook.domain.usecase

import com.example.hook.domain.model.User
import com.example.hook.domain.repository.AuthRepository
import com.example.hook.presentation.authentication.register.ValidationResult
import javax.inject.Inject

class ValidateInputFields @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(user: User): ValidationResult {
        return when {
            user.email.isBlank() || user.password.isBlank() || user.username.isBlank() || user.phoneNumber.isBlank() -> {
                ValidationResult.BlankFields()
            }

            user.password.length < 6 -> {
                ValidationResult.InvalidPassword()
            }

            auth.isUsernameTaken(user.username) -> {
                ValidationResult.UsernameTaken()
            }

            !auth.isValidPhoneNumber(user.phoneNumber) -> {
                ValidationResult.InvalidPhoneNumber()
            }

            !auth.isValidPassword(user.password) -> {
                ValidationResult.WeakPassword()
            }

            auth.isEmailTaken(user.email) -> {
                ValidationResult.EmailTaken()
            }

            auth.isPhoneNumberTaken(user.phoneNumber) -> {
                ValidationResult.PhoneNumberRegistered()
            }

            else -> {
                ValidationResult.Success
            }
        }
    }
}
