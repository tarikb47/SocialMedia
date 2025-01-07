package com.example.hook.presentation.home.chats.allchats.chat

import com.example.hook.data.remote.home.messages.MessagesRepository
import com.example.hook.domain.model.Message
import com.google.firebase.Timestamp


sealed class MessageState {
    data object Initial : MessageState()
    data object Loading : MessageState()
    data object MessageSent : MessageState()
    data class CurrentUserId(val userId: String) : MessageState()
    data class Error(val error: Throwable) : MessageState()
    data class MessagesLoaded(val messages: List<Message>) : MessageState()
    data class ContactDetailsFetched(val userProfile: MessagesRepository.UserProfile) : MessageState()
    data class UserActivityStatus(val status: String) : MessageState()


}