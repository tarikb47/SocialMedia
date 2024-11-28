package com.example.hook.presentation.authentication.register

sealed class ValidationResult {
    object Idle : ValidationResult()
    object Success : ValidationResult()
    data class BlankFields(val message: String = "Some fields are blank.") : ValidationResult()
    data class InvalidPassword(val message: String = "Password must be at least 6 characters.") : ValidationResult()
    data class UsernameTaken(val message: String = "Username is already taken.") : ValidationResult()
    data class InvalidPhoneNumber(val message: String = "Phone numbers must be 9 or 10 digits.") : ValidationResult()
    data class WeakPassword(val message: String = "Password must contain an uppercase letter, lowercase letter, and a number.") : ValidationResult()
    data class EmailTaken(val message: String = "Email is already registered.") : ValidationResult()
    data class PhoneNumberRegistered(val message: String = "Phone number is already linked to profile") : ValidationResult()
}