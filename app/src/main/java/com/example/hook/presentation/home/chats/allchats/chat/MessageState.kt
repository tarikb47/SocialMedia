package com.example.hook.presentation.home.chats.allchats.chat

import com.example.hook.domain.model.Message


sealed class MessageState {
    data object Initial : MessageState()
    data object Loading : MessageState()
    data object MessageSent : MessageState()
    data class CurrentUserId(val userId: String) : MessageState()
    data class Error(val error: Throwable) : MessageState()
    data class MessagesLoaded(val messages: List<Message>) : MessageState()

}