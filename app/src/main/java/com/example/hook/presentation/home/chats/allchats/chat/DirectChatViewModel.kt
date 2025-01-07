package com.example.hook.presentation.home.chats.allchats.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.remote.home.contacts.UserActivityRepository
import com.example.hook.data.remote.home.messages.MessagesRepository
import com.example.hook.domain.model.Message
import com.example.hook.presentation.authentication.helpers.TimeFormater
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectChatViewModel @Inject constructor(
    private val directChatRepo: MessagesRepository, private val localDb: UserRepositoryImpl,
    private val userStatus : UserActivityRepository
) : ViewModel() {
    private val _messageState: MutableStateFlow<MessageState> =
        MutableStateFlow(MessageState.Initial)
    val messageState = _messageState.asStateFlow()
    private val timeFormater = TimeFormater()
    fun sendMessage(users: List<String>, message: Message, user: String, contact: String) {
        viewModelScope.launch {
            directChatRepo.sendMessage(users, message, user, contact)
                .collectLatest { result ->
                    when (result) {
                        is Result.Error -> {
                            _messageState.value = MessageState.Error(result.error)
                            Log.d("Tarik", "sendMessage: ${result.error} ")
                        }

                        is Result.Success -> {
                            _messageState.value = MessageState.MessageSent
                            Log.d("Tarik", "sendMessage: ${result} ")
                        }
                    }
                }
        }
    }

    fun getCurrentUserVid() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = localDb.getUserEntityFromLocal()
            _messageState.value = MessageState.CurrentUserId(user.remoteVid)
        }

    }

    fun fetchMessages(currentUser: String, contact: String) {
        viewModelScope.launch {

            directChatRepo.fetchMessages(currentUser, contact).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        _messageState.value = MessageState.Error(result.error)
                    }

                    is Result.Success -> {
                        _messageState.value = MessageState.MessagesLoaded(result.data)
                    }
                }
            }
        }
    }

    fun listenForNewMessages(currentUser: String, contactId: String) {
        viewModelScope.launch {
            directChatRepo.listenForNewMessages(currentUser, contactId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val currentMessages =
                            (_messageState.value as? MessageState.MessagesLoaded)?.messages
                                ?: emptyList()
                        val updatedMessages =
                            (currentMessages + result.data).distinctBy { it.timestamp }
                                .sortedBy { it.timestamp }
                        _messageState.value = MessageState.MessagesLoaded(updatedMessages)
                    }

                    is Result.Error -> _messageState.value = MessageState.Error(result.error)
                }
            }
        }
    }

    fun getChatDetails(contact: String) {
        viewModelScope.launch {
            directChatRepo.getChatDetails(contact).collectLatest { result ->
                when (result) {
                    is Result.Error -> {}
                    is Result.Success -> _messageState.value =
                        MessageState.ContactDetailsFetched(result.data)
                }
            }
        }
    }
    fun observeActivity(contactID: String) {
        viewModelScope.launch {
            userStatus.observeUserStatus(contactID).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        _messageState.value = MessageState.UserActivityStatus("Unknown")
                    }
                    is Result.Success -> {
                        val activityStatus = result.data
                        _messageState.value = MessageState.UserActivityStatus(
                            when (activityStatus) {
                                is Boolean -> {
                                    if (activityStatus) "Online" else "Offline"
                                }
                                is Timestamp -> {
                                    timeFormater.getRelativeTime(activityStatus.toDate().time)
                                }

                                else -> {"Unknown"}
                            }
                        )
                    }
                }
            }
        }
    }


}
