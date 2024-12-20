package com.example.hook.presentation.home.contacts.addContact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.enums.FieldType
import com.example.hook.common.exception.BlankFieldException
import com.example.hook.common.exception.UnexpectedErrorException
import com.example.hook.common.result.Result
import com.example.hook.data.local.entity.ContactEntity
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.data.remote.home.contacts.ContactsRepository
import com.example.hook.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val repo: FirebaseAuthDataSource,
    private val inputDetermination : InputFieldHelper,
    private val contactRepo : ContactsRepository
) : ViewModel() {

    private val _contactState = MutableStateFlow<ContactState>(ContactState.Idle)
    val contactState: StateFlow<ContactState> get() = _contactState

    fun checkContact(fieldValue: String, nickname : String) {
        _contactState.value = ContactState.Loading
        val fieldType = inputDetermination.determineInput(fieldValue)
        if (fieldType == null) {
            _contactState.value = ContactState.InputError(BlankFieldException())
            return
        }
        viewModelScope.launch(Dispatchers.IO){
            repo.getContactByField(fieldValue, fieldType).collectLatest { result->
                when(result){
                    is Result.Error -> _contactState.value = ContactState.Error(result.error)
                    is Result.Success -> addContact(result.data, nickname)
                }
            }

        }
    }
    fun addContact(contact: Contact, nickname: String){
        viewModelScope.launch(Dispatchers.IO) {
            contactRepo.saveContactToRemote(contact.copy(username = nickname.ifEmpty { contact.username })).collectLatest { result ->
                when (result) {
                    is Result.Error -> ContactState.Error(result.error)
                    is Result.Success -> {
                        _contactState.value = ContactState.Success
                        contactRepo.saveContactToLocal(
                            ContactEntity(
                                userId = contact.userId,
                                username = nickname.ifEmpty { contact.username },
                                phoneNumber = contact.phoneNumber,
                                email = contact.email,
                                photoUrl = contact.photoUrl
                            )
                        )
                    }
                }
            }
        }


    }

    fun clearState() {
        _contactState.value = ContactState.Idle
    }

}
