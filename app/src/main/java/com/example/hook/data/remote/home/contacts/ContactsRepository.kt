package com.example.hook.data.remote.home.contacts

import android.util.Log
import com.example.hook.common.exception.UserNotLoggedInException
import com.example.hook.common.ext.asFlow
import com.example.hook.common.ext.mapSuccess
import com.example.hook.common.result.Result
import com.example.hook.data.local.UserRepositoryImpl
import com.example.hook.data.local.dao.ContactDao
import com.example.hook.data.local.dao.UserDao
import com.example.hook.data.local.dao.UserDao_Impl
import com.example.hook.data.local.entity.ContactEntity
import com.example.hook.data.remote.authentication.FirebaseAuthDataSource
import com.example.hook.domain.model.Contact
import com.example.hook.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contactDao: ContactDao,
    private val auth: FirebaseAuth,
    private val repo: FirebaseAuthDataSource,
    private val userDao: UserRepository
) {

    suspend fun saveContactToRemote(contact: Contact): Flow<Result<Unit>> {
        val user = userDao.getUserFromLocal(1).id
        return firestore.collection(USERS_COLLECTION)
            .document(user)
            .collection(CONTACTS_COLLECTION)
            .document(contact.userId)
            .set(contact)
            .asFlow()
            .mapSuccess {  }
    }

    suspend fun saveContactToLocal(contact: ContactEntity) {
        contactDao.saveContact(contact)
    }

    suspend fun getContactsFromRemote(): Flow<Result<List<Contact>>> {
        val user = userDao.getUserFromLocal(1).id
        return firestore.collection(USERS_COLLECTION)
            .document(user)
            .collection(CONTACTS_COLLECTION)
            .get()
            .asFlow()
            .mapSuccess { querySnapshot ->
                querySnapshot.documents.map { document ->
                    Contact(
                        userId = document.id,
                        username = document.getString(USERNAME),
                        phoneNumber = document.getString(PHONE_NUMBER),
                        email = document.getString(EMAIL),
                        photoUrl = document.getString(PHOTO_URL)
                    )
                }
            }
    }

    suspend fun getContactsFromLocal(): List<ContactEntity> {
        return contactDao.getAllContacts()
    }

    suspend fun syncContacts() {
        getContactsFromRemote().collectLatest { result ->
            if (result is Result.Success) {
                result.data.forEach { contact ->
                    saveContactToLocal(
                        ContactEntity(
                            userId = contact.userId,
                            username = contact.username,
                            phoneNumber = contact.phoneNumber,
                            email = contact.email,
                            photoUrl = contact.photoUrl

                        )
                    )
                }
            }
        }
    }
    suspend fun deleteContacts(){
        contactDao.deleteALlContacts()
    }
    fun deleteContactFromRemote(userId: String, contactId: String): Flow<Result<Unit>> {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(CONTACTS_COLLECTION)
            .document(contactId)
            .delete().asFlow().mapSuccess { Unit }
    }

    suspend fun deleteContactFromLocal(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun getContactFromLocalById(contactId: String): ContactEntity? {
        return contactDao.getContactById(contactId)
    }




    companion object {
        private const val CONTACTS_COLLECTION = "contacts"
        private const val USERS_COLLECTION = "Users"
        private const val USERNAME = "firstName"
        private const val PHONE_NUMBER = "phoneNumber"
        private const val EMAIL = "email"
        private const val PHOTO_URL = "photoUrl"
    }
}


