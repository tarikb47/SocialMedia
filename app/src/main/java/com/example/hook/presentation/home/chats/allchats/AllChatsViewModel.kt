package com.example.hook.presentation.home.chats.allchats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.data.remote.home.messages.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.remote.home.contacts.ContactsRepository
import com.example.hook.domain.model.Chat
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class AllChatsViewModel @Inject constructor(
    private val messagesRepo: MessagesRepository,
    private val contactRepo: ContactsRepository,
    private val localDb: UserRepositoryImpl
) : ViewModel() {

    private val _chatsState: MutableStateFlow<ChatsState> =
        MutableStateFlow(ChatsState.Initial)
    val chatsState = _chatsState.asStateFlow()

    fun fetchChats(currentUserVid: String) {
        _chatsState.value = ChatsState.Loading
        viewModelScope.launch {
            messagesRepo.retrieveChatsWithDetails(currentUserVid).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        _chatsState.value = ChatsState.Error(result.error)
                        Log.e("AllChatsViewModel", "Error fetching chats: ${result.error.message}")
                    }
                    is Result.Success -> {
                        _chatsState.value = ChatsState.UpdatedChats(result.data)
                        Log.d("AllChatsViewModel", "Chats updated: ${result.data}")
                    }
                }
            }
        }
    }

    fun getCurrentUserVid() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = localDb.getUserEntityFromLocal()
            _chatsState.value = ChatsState.CurrentUserId(user.remoteVid)
        }

    }


    sealed class ChatsState {
        data object Initial : ChatsState()
        data object Loading : ChatsState()
        data class CurrentUserId(val userId: String) : ChatsState()
        data class UpdatedChats(val chats: List<Chat>) : ChatsState()
        data class Error(val error: Throwable) : ChatsState()
    }
}
