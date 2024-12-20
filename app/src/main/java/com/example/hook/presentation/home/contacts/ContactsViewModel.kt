package com.example.hook.ui.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.result.Result
import com.example.hook.data.local.entity.ContactEntity
import com.example.hook.data.remote.home.contacts.ContactsRepository
import com.example.hook.presentation.home.contacts.RecyclerViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<RecyclerViewItem>>(emptyList())
    val contacts: StateFlow<List<RecyclerViewItem>> = _contacts

    private val _errorMessage = MutableStateFlow<String?>(null)
    private var errorMessage: StateFlow<String?> = _errorMessage

    /* fun fetchAndSaveContacts() {
         viewModelScope.launch(Dispatchers.IO) {
             contactsRepository.getContactsFromRemote().collectLatest { result ->
                 when (result) {
                     is Result.Error -> {
                         _errorMessage.value = "Failed to load contacts: ${result.error?.message}"
                     }
                     is Result.Success -> {
                         val remoteContacts = result.data.map { contact ->
                             RecyclerViewItem.ContactItem(
                                 ContactEntity(
                                     userId = contact.userId,
                                     username = contact.username,
                                     phoneNumber = contact.phoneNumber,
                                     email = contact.email,
                                     photoUrl = contact.photoUrl
                                 )
                             )
                         }
                         remoteContacts.forEach { item ->
                             contactsRepository.saveContactToLocal(item.contact)
                         }
                         _contacts.value = remoteContacts
                     }
                 }
             }
         }*/

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactsRepository.getContactsFromLocal()
            if (contacts.isEmpty()) {
                syncContacts()
                _contacts.value = contacts.map { contact ->
                    RecyclerViewItem.ContactItem(contact)
                }
            }
            else {
                _contacts.value = contacts.map { contact ->
                    RecyclerViewItem.ContactItem(contact)
                }
            }
        }
    }

    fun syncContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.getContactsFromRemote().collectLatest { remoteResult ->
                when (remoteResult) {
                    is Result.Error -> {
                        _errorMessage.value =
                            "Failed to load contacts: ${remoteResult.error?.message}"
                    }

                    is Result.Success -> {
                        val remoteContacts = remoteResult.data.map { contact ->
                            RecyclerViewItem.ContactItem(
                                ContactEntity(
                                    userId = contact.userId,
                                    username = contact.username,
                                    phoneNumber = contact.phoneNumber,
                                    email = contact.email,
                                    photoUrl = contact.photoUrl
                                )
                            )
                        }
                        remoteContacts.forEach { item ->
                            contactsRepository.saveContactToLocal(item.contact)
                        }
                    }
                }
            }
        }
    }


}
