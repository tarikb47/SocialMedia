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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    fun sendMessage(
        users: List<String>,
        message: Message,
        userId: String,
        contact: String
    ): Flow<Result<Unit>> = flow {
        val chatQuery = firestore.collection("Chats")
            .whereArrayContains("users", userId)
            .get()
            .await()

        val chatDocument = chatQuery.documents.firstOrNull { doc ->
            val chatUsers = doc.get("users") as? List<String>
            chatUsers?.contains(contact) == true
        }

        val chatRef = if (chatDocument != null) {
            firestore.collection("Chats").document(chatDocument.id)
        } else {
            val newChatRef = firestore.collection("Chats").document()
            val chatData = mapOf(
                "chatId" to newChatRef.id,
                "users" to users,
                "lastMessage" to message.text,
                "timestamp" to message.timestamp,
            )
            newChatRef.set(chatData, SetOptions.merge()).await()
            newChatRef
        }

        chatRef.collection("Messages").add(message).await()

        val chatData = mapOf(
            "lastMessage" to message.text,
            "timestamp" to message.timestamp,
        )
        chatRef.set(chatData, SetOptions.merge()).await()

        emit(Result.Success(Unit))
    }.catch { e ->
        e
    }




    fun sendExistingMessage(
        chatRef: DocumentReference,
        message: Message
    ): Flow<Result<Unit>> {
        Log.d("MessagesRepository", "Sending message to existing chat")
        return chatRef.collection("Messages").add(message).asFlow()
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
        val chatData = mapOf(
            "chatId" to chatRef.id,
            "users" to users,
            "lastMessage" to message.text,
            "timestamp" to message.timestamp,
        )
        return chatRef.set(chatData, SetOptions.merge()).asFlow()
            .flatMapLatest {
                sendExistingMessage(chatRef, message)
            }
            .catch { e ->
                emit(Result.Error(e))
            }
    }

    fun retrieveChats(userId: String): Flow<Result<List<Chat>>> {
        return firestore.collection("Chats")
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
        return firestore.collection("Users").document(userId).get().asFlow()
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

    fun fetchMessages(currentUser: String, contact: String): Flow<Result<List<Message>>> {
        return firestore.collection("Chats")
            .whereArrayContains("users", currentUser)
            .get()
            .asFlow()
            .flatMapLatest { result ->
                if (result is Result.Success) {
                    val querySnapshot = result.data
                    val chatDocument = querySnapshot.documents.firstOrNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.contains(contact) == true
                    }

                    if (chatDocument != null) {
                        val chatId = chatDocument.id
                        firestore.collection("Chats")
                            .document(chatId)
                            .collection("Messages")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .get()
                            .asFlow()
                            .mapSuccess { snapshot ->
                                snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(Message::class.java)
                                }
                            }
                    } else {
                        flowOf(Result.Success(emptyList()))
                    }
                } else {
                    flowOf(Result.Error(Exception("Error retrieving chats for the user.")))
                }
            }
            .catch { e ->
                Log.e("MessagesRepository", "Error fetching messages: ${e.message}")
                emit(Result.Error(e))
            }
    }

    fun listenForNewMessages(userId: String, contact: String): Flow<Result<List<Message>>> {
        return callbackFlow {
            val closeableListeners = mutableListOf<() -> Unit>()
            var registeredChatId: String? = null

            val chatQuery = firestore.collection("Chats")
                .whereArrayContains("users", userId)

            val queryRegistration = chatQuery.addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }

                val chatDocument = querySnapshot?.documents?.firstOrNull { doc ->
                    val users = doc.get("users") as? List<String>
                    users?.contains(contact) == true
                }

                if (chatDocument != null) {
                    val chatId = chatDocument.id

                    if (chatId == registeredChatId) {
                        return@addSnapshotListener
                    }

                    registeredChatId = chatId

                    val listenerRegistration = firestore.collection("Chats")
                        .document(chatId)
                        .collection("Messages")
                        .orderBy("timestamp", Query.Direction.ASCENDING)
                        .addSnapshotListener { snapshot, messageError ->
                            if (messageError != null) {
                                trySend(Result.Error(messageError))
                            } else {
                                val messages = snapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(Message::class.java)
                                } ?: emptyList()
                                trySend(Result.Success(messages))
                            }
                        }

                    closeableListeners.add { listenerRegistration.remove() }
                } else {
                    trySend(Result.Error(Exception("No chat found between $userId and $contact")))
                }
            }

            closeableListeners.add { queryRegistration.remove() }

            awaitClose {
                closeableListeners.forEach { it.invoke() }
            }
        }
    }



    data class UserProfile(
        val username: String = "",
        val photoUrl: String = ""
    )
}