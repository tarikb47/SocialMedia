package com.example.hook.presentation.home.chats.allchats.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.remote.home.messages.MessagesRepository
import com.example.hook.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectChatViewModel @Inject constructor(private val directChatRepo : MessagesRepository, private val localDb: UserRepositoryImpl) : ViewModel() {
    private val _messageState: MutableStateFlow<MessageState> =
        MutableStateFlow(MessageState.Initial)
    val messageState = _messageState.asStateFlow()
    fun sendMessage( users: List<String>, message: Message) {
        viewModelScope.launch {
            directChatRepo.sendMessage(users, message)
                .collectLatest { result ->
                    when (result) {
                        is Result.Error -> {_messageState.value = MessageState.Error(result.error)
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

     fun getCurrentUserVid ()  {
        viewModelScope.launch(Dispatchers.IO){val user = localDb.getUserEntityFromLocal()
            _messageState.value = MessageState.CurrentUserId(user.remoteVid)
        }

    }
    fun fetchMessages(currentUser: String, contact: String) {
        viewModelScope.launch {
            val chatId = listOf(currentUser, contact).sorted().joinToString("-")

            directChatRepo.fetchMessages(chatId).collectLatest { result ->
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
            val chatId = listOf(currentUser, contactId).sorted().joinToString("-")
            directChatRepo.listenForNewMessages(chatId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val currentMessages = (_messageState.value as? MessageState.MessagesLoaded)?.messages ?: emptyList()
                        val updatedMessages = currentMessages.toMutableList().apply { add(result.data) }
                        _messageState.value = MessageState.MessagesLoaded(updatedMessages)
                    }
                    is Result.Error -> _messageState.value = MessageState.Error(result.error)
                }
            }
        }
    }
}