package com.example.hook.presentation.home.contacts.addContact

import com.example.hook.common.enums.FieldType
import com.example.hook.common.exception.BlankFieldsException
import javax.inject.Inject

class InputFieldHelper @Inject constructor(){
    fun determineInput (input : String) : FieldType?{
       return when{
           input.isBlank() -> null
           isPhoneNumber(input) -> FieldType.PHONE_NUMBER
           isEmail(input) -> FieldType.EMAIL
           else -> FieldType.USERNAME
        }

    }

    private fun isPhoneNumber(phone: String): Boolean {
        return phone.matches(VALID_PHONE_NUMBER_PATTERN)
    }

    fun isEmail(email: String): Boolean {
        return email.matches(VALID_EMAIL_PATTERN)
    }




    companion object {
        private val VALID_EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        private val VALID_PHONE_NUMBER_PATTERN = Regex("^\\+\\d{7,15}$")
    }
}
