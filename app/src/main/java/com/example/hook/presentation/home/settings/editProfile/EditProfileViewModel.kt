package com.example.hook.presentation.home.settings.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.remote.home.contacts.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val db: UserRepositoryImpl,
    private val contactsDb : ContactsRepository
) : ViewModel() {

    fun clearData() {
        viewModelScope.launch {
            db.clearLocalUserData()
            contactsDb.deleteContacts()
        }
    }
}