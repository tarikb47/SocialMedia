package com.example.hook.data.remote.home.contacts

import android.util.Log
import com.example.hook.common.ext.asFlow
import com.example.hook.common.ext.mapError
import com.example.hook.common.ext.mapSuccess
import com.example.hook.common.result.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class UserActivityRepository @Inject constructor(
    private val database: FirebaseDatabase,
     private val auth: FirebaseAuth,
) {

    private val currentUser
        get() = auth.currentUser
    private val userStatusRef: DatabaseReference
        get() = database.getReference("status")

    fun userOnline(): Flow<Result<Unit>> = currentUser?.let { user ->
        val statusRef = userStatusRef.child(user.uid)
        statusRef.setValue(
            mapOf(
                "online" to true,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        ).asFlow().mapSuccess { Unit }
    } ?: flowOf(Result.Error(Exception("User not authenticated")))

    fun userOffline(): Flow<Result<Unit>> = currentUser?.let { user ->
        val statusRef = userStatusRef.child(user.uid)
        statusRef.setValue(
            mapOf(
                "online" to false,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        ).asFlow().mapSuccess { Unit }
    } ?: flowOf(Result.Error(Exception("User not authenticated")))



    fun observeUserStatus(userId: String): Flow<Result<Any>> {
        val statusRef = userStatusRef.child(userId)

        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(Result.Success(snapshot)).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.Error(error.toException())).isFailure
                }
            }

            statusRef.addValueEventListener(listener)

            awaitClose {
                statusRef.removeEventListener(listener)
            }
        }
            .mapSuccess { snapshot ->
                val isOnline = snapshot.child("online").getValue(Boolean::class.java) ?: false
                if (isOnline) {
                    true
                } else {
                    snapshot.child("lastSeen").getValue(Long::class.java)?.let { millis ->
                        Timestamp(millis / 1000, (millis % 1000).toInt() * 1000000)
                    } ?: "Last seen: Not available"
                }
            }
            .mapError { error ->
                Throwable("Failed to observe user status: ${error.message}")
            }
    }



}







