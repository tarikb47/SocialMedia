package com.example.hook.presentation.home.settings.editProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.data.remote.home.contacts.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val db: UserRepositoryImpl,
    private val contactsDb : ContactsRepository,
    private val userRepo : FirebaseAuthDataSource
) : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState
    fun clearData() {
        viewModelScope.launch {
            db.clearLocalUserData()
            contactsDb.deleteContacts()
        }
    }
    fun uploadPhotoToFirebase (photoUri : Uri){
        viewModelScope.launch {
            userRepo.uploadProfilePicture(photoUri).collectLatest { result ->
                when(result) {
                    is Result.Error -> _profileState.value = ProfileState.Error(result.error)
                    is Result.Success -> _profileState.value = ProfileState.PhotoChanged
                }
            }

        }
    }


}