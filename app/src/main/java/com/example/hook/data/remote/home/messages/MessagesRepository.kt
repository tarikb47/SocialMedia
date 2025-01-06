package com.example.hook.data.remote.home.messages

import android.util.Log
import com.example.hook.common.exception.MessageSentException
import com.example.hook.common.ext.asFlow
import com.example.hook.common.ext.mapError
import com.example.hook.common.ext.mapSuccess
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.ContactDao
import com.example.hook.data.local.entity.ContactEntity
import com.example.hook.domain.model.Chat
import com.example.hook.domain.model.Message
import com.example.hook.domain.model.User
import com.example.hook.presentation.home.chats.allchats.chat.MessageState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    fun sendMessage(
        users: List<String>,
        message: Message
    ): Flow<Result<Unit>> {
        val chatId = users.sorted().joinToString("-")
        Log.d("MessagesRepository", "Chat ID generated: $chatId")
        val chatRef = firestore.collection("chats").document(chatId)

        return chatRef.get().asFlow()
            .flatMapLatest { result ->
                if (result is Result.Success) {
                    val documentSnapshot = result.data
                    if (documentSnapshot.exists()) {
                        Log.d("MessagesRepository", "Chat already exists, sending message")
                        sendExistingMessage(chatRef, message)
                    } else {
                        Log.d("MessagesRepository", "Chat does not exist, creating a new chat")
                        createNewChat(users, message, chatRef)
                    }
                } else {
                    flowOf(Result.Error(Exception("Error checking chat existence.")))
                }
            }
            .catch { e ->
                Log.e("MessagesRepository", "Error in sendMessage: ${e.message}")
                emit(Result.Error(e))
            }
    }

    fun sendExistingMessage(
        chatRef: DocumentReference,
        message: Message
    ): Flow<Result<Unit>> {
        Log.d("MessagesRepository", "Sending message to existing chat")
        return chatRef.collection("messages").add(message).asFlow()
            .flatMapLatest {
                val chatData = mapOf(
                    "lastMessage" to message.text,
                    "timestamp" to message.timestamp
                )
                chatRef.set(chatData, SetOptions.merge()).asFlow()
            }
            .mapSuccess {
                Log.d("MessagesRepository", "Message sent and chat updated successfully")
                Unit
            }
            .mapError { error ->
                Log.e("MessagesRepository", "Error sending message: ${error.message}")
                error
            }
    }

    fun createNewChat(
        users: List<String>,
        message: Message,
        chatRef: DocumentReference
    ): Flow<Result<Unit>> {
        Log.d("MessagesRepository", "Creating new chat")
        val otherUserId = users.first { it != message.senderId }

        return firestore.collection("users").document(otherUserId).get().asFlow()
            .flatMapLatest { result ->
                if (result is Result.Success) {
                    val chatData = mapOf(
                        "users" to users,
                        "lastMessage" to message.text,
                        "timestamp" to message.timestamp,
                    )
                    chatRef.set(chatData, SetOptions.merge()).asFlow()
                        .flatMapLatest {
                            sendExistingMessage(chatRef, message)
                        }
                } else {
                    flowOf(Result.Error(Exception("Error fetching user profile.")))
                }
            }
            .catch { e ->
                Log.e("MessagesRepository", "Error creating new chat: ${e.message}")
                emit(Result.Error(e))
            }
    }

    fun retrieveChats(userId: String): Flow<Result<List<Chat>>> {
        return firestore.collection("chats")
            .whereArrayContains("users", userId)
            .get().asFlow()
            .mapSuccess { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Chat::class.java)?.apply { chatId = document.id }
                }
            }
            .mapError { error ->
                Log.e("MessagesRepository", "Error retrieving chats: ${error.message}")
                error
            }
    }

    fun getChatDetails(userId: String): Flow<Result<UserProfile>> {
        return firestore.collection("users").document(userId).get().asFlow()
            .mapSuccess { documentSnapshot ->
                val userProfile = documentSnapshot.toObject(UserProfile::class.java)
                userProfile!!
            }
            .mapError { error ->
                error
            }
    }

    fun retrieveChatsWithDetails(userId: String): Flow<Result<List<Chat>>> {
        return retrieveChats(userId)
            .flatMapLatest { result ->
                when (result) {
                    is Result.Error -> flowOf(Result.Error(Exception("Error retrieving chats.")))
                    is Result.Success -> {
                        val chats = result.data
                        val chatDetailsFlows = chats.map { chat ->
                            val otherUserId = chat.users.first { it != userId }

                            getChatDetails(otherUserId)
                                .flatMapLatest { userProfileResult ->
                                    when (userProfileResult) {
                                        is Result.Error -> {
                                            flowOf(chat.apply {
                                                username = "Unknown"
                                                photoUrl = ""
                                            })
                                        }

                                        is Result.Success -> {
                                            val userProfile = userProfileResult.data
                                            flowOf(chat.apply {
                                                username = userProfile.username
                                                photoUrl = userProfile.photoUrl
                                            })
                                        }
                                    }
                                }
                        }
                        combine(chatDetailsFlows) { updatedChats ->
                            Result.Success(updatedChats.map { it })
                        }
                    }
                }
            }
            .catch { e ->
                Log.e("MessagesRepository", "Error retrieving chats with details: ${e.message}")
                emit(Result.Error(e))
            }
    }

    fun fetchMessages(chatId: String): Flow<Result<List<Message>>> {
        return firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .asFlow()
            .mapSuccess { snapshot ->
                if (!snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    }
                    messages
                } else {
                    error("")
                }
            }
    }

    fun listenForNewMessages(chatId: String): Flow<Result<Message>> {
        return callbackFlow {
            val listenerRegistration = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.Error(error)) // Send error if snapshot listener fails
                    } else if (snapshot != null) {
                        snapshot.documentChanges.forEach { docChange ->
                            if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                val message = docChange.document.toObject(Message::class.java)
                                message.let {
                                    trySend(Result.Success(it))
                                }
                            }
                        }
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }



    data class UserProfile(
        val username: String = "",
        val photoUrl: String = ""
    )
}